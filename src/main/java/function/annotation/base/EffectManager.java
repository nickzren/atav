package function.annotation.base;

import function.annotation.base.Enum.Impact;
import global.Data;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.CommandManager;
import utils.CommonCommand;
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
    private static HashMap<Integer, String> id2ImpactMap = new HashMap<>();
    private static HashMap<String, Integer> impactEffect2IdMap = new HashMap<>();
    // potential problem here for the same effect name 
    private static HashMap<String, Integer> effect2IdMap = new HashMap<>();

    // user input values
    private static HashSet<Impact> inputImpactSet = new HashSet<>();
    private static HashSet<String> inputEffectSet = new HashSet<>();
    private static HashSet<Integer> inputExcludeEffectIdSet = new HashSet<>();

    private static final String HIGH_IMPACT = "('HIGH')";
    private static final String MODERATE_IMPACT = "('HIGH'),('MODERATE')";
    private static final String LOW_IMPACT = "('HIGH'),('MODERATE'),('LOW')";
    private static final String MODIFIER_IMPACT = "('HIGH'),('MODERATE'),('LOW'),('MODIFIER')";

    public static HashSet<Integer> MISSENSE_EFFECT_ID_SET = new HashSet<>();
    public static HashSet<Integer> LOF_EFFECT_ID_SET = new HashSet<>();
    public static HashSet<String> LOF_EFFECT_SET = new HashSet<>();

    private static boolean isUsed = false;

    public static void init() throws SQLException {
        if (CommonCommand.isNonDBAnalysis) {
            return;
        }

        initDefaultEffectSet();

        initLOFEffectIDSet();

        initExcludeInputEffectSet();

        initInputImpactSet();

        initInputEffectSet();
    }

    private static void initTempTable() {
        try {
            Statement stmt = DBManager.createStatementByConcurReadOnlyConn();

            // create table
            String sqlQuery = "CREATE TEMPORARY TABLE " + TMP_EFFECT_ID_TABLE + " ("
                    + "input_effect_id tinyint(3) NOT NULL, "
                    + "PRIMARY KEY (input_effect_id));";

            stmt.executeUpdate(sqlQuery);
            stmt.closeOnCompletion();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("CREATE TEMPORARY TABLE " + TMP_EFFECT_ID_TABLE + " ("
                + "input_effect_id tinyint(3) NOT NULL, "
                + "PRIMARY KEY (input_effect_id));");

        AnnotationLevelFilterCommand.impactInput = "HIGH,LOW";

        initInputImpactSet();

        System.out.println(inputImpactSet.stream().map(impact -> "'" + impact.name() + "'").collect(Collectors.joining(",")));
    }

    private static void initDefaultEffectSet() {
        try {
            String sql = "SELECT * FROM effect_ranking";
            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String effect = rs.getString("effect");
                Impact impact = Impact.valueOf(rs.getString("impact"));
                String impactEffect = impact + ":" + effect;

                id2ImpactMap.put(id, impact.name());
                id2EffectMap.put(id, effect);
                impactEffect2IdMap.put(impactEffect, id);
                effect2IdMap.put(effect, id);

                if (effect.startsWith("missense_variant") ||
                        effect.equals("disruptive_inframe_deletion") ||
                        effect.equals("disruptive_inframe_insertion") ||
                        effect.equals("conservative_inframe_deletion") ||
                        effect.equals("conservative_inframe_insertion")) {
                    MISSENSE_EFFECT_ID_SET.add(id);
                }
            }

            rs.close();
            preparedStatement.close();
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

    private static void initInputImpactSet() throws SQLException {
        String inputImpact = AnnotationLevelFilterCommand.impactInput;

        if (inputImpact.isEmpty()) {
            return;
        }

        for (String impact : inputImpact.split(",")) {
            inputImpactSet.add(Impact.valueOf(impact));
        }
    }

    private static void initInputEffectSet() throws SQLException {
        String inputEffect = AnnotationLevelFilterCommand.effectInput;
        String inputImpact = AnnotationLevelFilterCommand.impactInput;

        if (CommandManager.isFileExist(inputEffect)) {
            inputEffect = initEffectFromFile(inputEffect);
        }

        initEffectSet(inputImpact, inputEffect);

        initLowestImpact();
    }

    private static void initExcludeInputEffectSet() throws SQLException {
        String excludeEffectInput = AnnotationLevelFilterCommand.excludeEffectInput;

        for (String impactEffect : excludeEffectInput.split(",")) {
            if (impactEffect2IdMap.containsKey(impactEffect)) {
                LogManager.writeAndPrint("Excluded effect: " + impactEffect);
                inputExcludeEffectIdSet.add(impactEffect2IdMap.get(impactEffect));
            }
        }
    }

    private static String initEffectFromFile(String inputEffect) {
        String effectFilePath = inputEffect;

        try ( Stream<String> stream = Files.lines(Paths.get(effectFilePath))) {
            inputEffect = stream.map(line -> line.replaceAll("( )+", ""))
                    .collect(Collectors.joining(","));
        } catch (IOException e) {
            ErrorManager.send(e);
        }

        return inputEffect;
    }

    /*
        1. --impact and --effect unused
        2. --impact used but --effect unused
        3. --impact unused but --effect used
        4. --impact and --effect used
     */
    private static void initEffectSet(String inputImpact, String inputEffect) throws SQLException {
        // --impact and --effect unused
        if (inputImpact.isEmpty() && inputEffect.isEmpty()) {
            return;
        }

        initTempTable();

        Statement stmt = DBManager.createStatementByConcurReadOnlyConn();

        // --impact used but --effect unused
        if (!inputImpact.isEmpty() && inputEffect.isEmpty()) {
            String impactSQL = "where impact in ("
                    + inputImpactSet.stream().map(impact -> "'" + impact.name() + "'").collect(Collectors.joining(",")) + ");";

            stmt.executeUpdate("INSERT INTO tmp_effect_id(input_effect_id) select id from effect_ranking " + impactSQL);

            try {
                String sql = "select id,effect from effect_ranking " + impactSQL;
                PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    // apply --exclude-effect filter
                    if (!inputExcludeEffectIdSet.isEmpty()
                            && inputExcludeEffectIdSet.contains(rs.getInt("id"))) {
                        continue;
                    }

                    String effect = rs.getString("effect");
                    inputEffectSet.add(effect);
                }

                rs.close();
                preparedStatement.close();
            } catch (Exception e) {
                ErrorManager.send(e);
            }
        } else {
            // --impact unused but --effect used
            // --impact and --effect used
            for (String impactEffect : inputEffect.split(",")) { // input impactEffect format: lowestImpact:effect
                if (!impactEffect2IdMap.containsKey(impactEffect)) {
                    LogManager.writeAndPrint("Invalid effect: " + impactEffect);
                    continue;
                }

                // apply --exclude-effect filter
                if (!inputExcludeEffectIdSet.isEmpty()
                        && inputExcludeEffectIdSet.contains(impactEffect2IdMap.get(impactEffect))) {
                    continue;
                }

                Impact impact = Impact.valueOf(impactEffect.split(":")[0]);
                String effect = impactEffect.split(":")[1];

                // skip effect that not match input impact
                if (!inputImpact.isEmpty() && !inputImpactSet.contains(impact)) {
                    continue;
                }

                stmt.executeUpdate("INSERT IGNORE INTO tmp_effect_id values ("
                        + impactEffect2IdMap.get(impactEffect) + ");");

                inputImpactSet.add(impact);
                inputEffectSet.add(effect);
            }
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
                    + "PRIMARY KEY (input_impact)) ;");

            // insert values
            stmt.executeUpdate("INSERT IGNORE INTO tmp_impact values " + impactList4SQL);

            stmt.closeOnCompletion();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getImpactById(int id) {
        return id2ImpactMap.get(id);
    }
    
    public static String getEffectById(int id) {
        return id2EffectMap.get(id);
    }

    public static int getIdByEffect(String effect) {
        return effect2IdMap.get(effect);
    }

    public static boolean isMISSENSE(int effectID) {
        return MISSENSE_EFFECT_ID_SET.contains(effectID);
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
