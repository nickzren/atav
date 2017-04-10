package function.annotation.base;

import function.annotation.base.Enum.Impact;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
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

    // system defualt values
    private static HashMap<Integer, String> id2EffectMap = new HashMap<>();
    private static HashMap<String, Integer> impactEffect2IdMap = new HashMap<>();

    // user input values
    private static HashSet<Integer> inputEffectIdSet = new HashSet<>();
    private static HashSet<Impact> inputImpactSet = new HashSet<>();

    private static final String HIGH_IMPACT = "('HIGH')";
    private static final String MODERATE_IMPACT = "('HIGH'),('MODERATE')";
    private static final String LOW_IMPACT = "('HIGH'),('MODERATE'),('LOW')";
    private static final String MODIFIER_IMPACT = "('HIGH'),('MODERATE'),('LOW'),('MODIFIER')";

    private static boolean isUsed = false;

    public static void init() {
        initDefaultEffectSet();

        initInputEffectSet();
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
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initInputEffectSet() {
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

    private static void initEffectSet(String inputEffect) {
        if (inputEffect.isEmpty()) {
            return;
        }

        for (String impactEffect : inputEffect.split(",")) { // input impactEffect format: lowestImpact:effect
            if (!impactEffect2IdMap.containsKey(impactEffect)) {
                LogManager.writeAndPrint("Invalid effect: " + impactEffect);
                continue;
            }

            inputEffectIdSet.add(impactEffect2IdMap.get(impactEffect));
            inputImpactSet.add(Impact.valueOf(impactEffect.split(":")[0]));
        }
    }

    private static void initLowestImpact() {
        if (!inputEffectIdSet.isEmpty()) {
            isUsed = true;

            Impact lowestImpact = Impact.HIGH;

            for (Impact impact : inputImpactSet) {
                if (impact.getValue() < impact.getValue()) {
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
                    ErrorManager.print("Unknown impact: " + lowestImpact);
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
            String sqlQuery = "CREATE TEMPORARY TABLE impact("
                    + "impact enum('HIGH','MODERATE','LOW','MODIFIER') NOT NULL, "
                    + "PRIMARY KEY (impact)) ENGINE=TokuDB;";

            stmt.executeUpdate(sqlQuery);

            // insert values
            sqlQuery = "INSERT INTO impact values " + impactList4SQL;

            stmt.executeUpdate(sqlQuery);
            
            stmt.closeOnCompletion();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getEffectIdList4SQL() {
        StringBuilder sb = new StringBuilder();

        boolean isFirst = true;

        for (int id : inputEffectIdSet) {
            if (isFirst) {
                isFirst = false;
                sb.append("(");
            } else {
                sb.append(",");
            }

            sb.append(id);
        }

        sb.append(")");

        return sb.toString();
    }

    public static String getEffectById(int id) {
        return id2EffectMap.get(id);
    }

    public static boolean isUsed() {
        return isUsed;
    }
}
