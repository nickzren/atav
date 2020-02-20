package function.annotation.base;

import function.annotation.base.Enum.Impact;
import global.Data;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.CommandManager;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class EffectManager {

    public static final String TMP_EFFECT_ID_TABLE = "tmp_effect_id";
    public static final String TMP_IMPACT_TABLE = "tmp_impact";

    public static final String LOF_EFFECT_FILE_PATH = Data.ATAV_HOME + "data/effect/lof.txt";
    
    // system defualt values
    private static HashMap<Integer, String> id2EffectMap = new HashMap<>();
    private static HashMap<String, Integer> impactEffect2IdMap = new HashMap<>();

    // user input values
    private static HashSet<Impact> inputImpactSet = new HashSet<>();
    private static HashSet<String> inputEffectSet = new HashSet<>();

    private static final String HIGH_IMPACT = "('HIGH')";
    private static final String MODERATE_IMPACT = "('HIGH'),('MODERATE')";
    private static final String LOW_IMPACT = "('HIGH'),('MODERATE'),('LOW')";
    private static final String MODIFIER_IMPACT = "('HIGH'),('MODERATE'),('LOW'),('MODIFIER')";

    public static int MISSENSE_VARIANT_ID;
    public static HashSet<Integer> LOF_EFFECT_ID_SET = new HashSet<>();
    public static HashSet<String> LOF_EFFECT_SET = new HashSet<>();
    
    private static boolean isUsed = false;

    public static void init() throws SQLException {
        initDefaultEffectSet();
        
        initLOFEffectIDSet();

        initInputEffectSet();
    }

    private static void initTempTable() {
        try {
            Statement stmt = DBManager.createStatementByConcurReadOnlyConn();

            // create table
            String sqlQuery = "CREATE TEMPORARY TABLE " + TMP_EFFECT_ID_TABLE + " ("
                    + "input_effect_id tinyint(3) NOT NULL, "
                    + "PRIMARY KEY (input_effect_id)) ENGINE=MEMORY;";

            stmt.executeUpdate(sqlQuery);
            stmt.closeOnCompletion();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initDefaultEffectSet() {
        try {
            String sql = "SELECT * FROM effect_ranking";

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String effect = rs.getString("effect");
                Impact impact = Impact.valueOf(rs.getString("impact"));
                String impactEffect = impact + ":" + effect;

                id2EffectMap.put(id, effect);
                impactEffect2IdMap.put(impactEffect, id);

                if (effect.equals("missense_variant")) {
                    MISSENSE_VARIANT_ID = id;
                }
            }
            
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    private static void initLOFEffectIDSet() {
        String lofEffect = initEffectFromFile(LOF_EFFECT_FILE_PATH);
        
        for (String impactEffect : lofEffect.split(",")) {
            LOF_EFFECT_ID_SET.add(impactEffect2IdMap.get(impactEffect));
            LOF_EFFECT_SET.add(impactEffect.split(":")[1]);
        }
    }

    private static void initInputEffectSet() throws SQLException {
        String inputEffect = AnnotationLevelFilterCommand.effectInput.replaceAll("( )+", "");

        if (CommandManager.isFileExist(inputEffect)) {
            inputEffect = initEffectFromFile(inputEffect);
        }

        initEffectSet(inputEffect);

        initLowestImpact();
    }

    private static String initEffectFromFile(String inputEffect) {
        String effectFilePath = inputEffect;

        try (Stream<String> stream = Files.lines(Paths.get(effectFilePath))) {
            inputEffect = stream.map(line -> line.replaceAll("( )+", ""))
                    .collect(Collectors.joining(","));
        } catch (IOException e) {
            ErrorManager.send(e);
        }

        return inputEffect;
    }

    private static void initEffectSet(String inputEffect) throws SQLException {
        if (inputEffect.isEmpty()) {
            return;
        }

        initTempTable();

        Statement stmt = DBManager.createStatementByConcurReadOnlyConn();

        for (String impactEffect : inputEffect.split(",")) { // input impactEffect format: lowestImpact:effect
            if (!impactEffect2IdMap.containsKey(impactEffect)) {
                LogManager.writeAndPrint("Invalid effect: " + impactEffect);
                continue;
            }

            stmt.executeUpdate("INSERT IGNORE INTO tmp_effect_id values ("
                    + impactEffect2IdMap.get(impactEffect) + ");");

            inputImpactSet.add(Impact.valueOf(impactEffect.split(":")[0]));
            inputEffectSet.add(impactEffect.split(":")[1]);
        }

        stmt.closeOnCompletion();
    }

    private static void initLowestImpact() {
        if (!inputImpactSet.isEmpty()) {
            isUsed = true;

            Impact lowestImpact = Impact.HIGH;

            for (Impact impact : inputImpactSet) {
                if (lowestImpact.getValue() < impact.getValue()) {
                    lowestImpact = impact;
                }
            }

            switch (lowestImpact) {
                case HIGH:
                    initImpactTable(HIGH_IMPACT);
                    break;
                case MODERATE:
                    initImpactTable(MODERATE_IMPACT);
                    break;
                case LOW:
                    initImpactTable(LOW_IMPACT);
                    break;
                case MODIFIER:
                    initImpactTable(MODIFIER_IMPACT);
                    break;
                default:
                    ErrorManager.print("Unknown impact: " + lowestImpact, ErrorManager.INPUT_PARSING);
            }
        } else {
            // when--effect not used
            initImpactTable(MODIFIER_IMPACT);
        }
    }

    private static void initImpactTable(String impactList4SQL) {
        try {
            Statement stmt = DBManager.createStatement();

            // create table
            stmt.executeUpdate(
                    "CREATE TEMPORARY TABLE " + TMP_IMPACT_TABLE + " ("
                    + "input_impact enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL, "
                    + "PRIMARY KEY (input_impact)) ENGINE=MEMORY;");

            // insert values
            stmt.executeUpdate("INSERT IGNORE INTO tmp_impact values " + impactList4SQL);

            stmt.closeOnCompletion();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getEffectById(int id) {
        return id2EffectMap.get(id);
    }

    public static boolean isLOF(int effectID) {
        return LOF_EFFECT_ID_SET.contains(effectID);
    }
    
    public static boolean isLOF(String effect) {
        return LOF_EFFECT_SET.contains(effect);
    }
    
    public static boolean isEffectContained(String effect) {
        return inputEffectSet.isEmpty() || inputEffectSet.contains(effect);
    }
    
    public static boolean isUsed() {
        return isUsed;
    }
}
