package function.annotation.base;

import function.annotation.base.Enum.Impact;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
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
    private static HashMap<String, Integer> effect2IdMap = new HashMap<>();
    private static HashMap<String, Impact> effect2ImpactMap = new HashMap<>();

    // user input values
    private static HashSet<String> inputEffectSet = new HashSet<>();
    private static HashSet<Integer> inputIdSet = new HashSet<>();
    private static HashSet<Impact> inputImpactSet = new HashSet<>();

    private static Impact lowestInputImpact = Impact.HIGH; // higher impact value, lower impact affect - HIGH(1), MODERATE(2), LOW(3), MODIFIER(4)

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

                id2EffectMap.put(id, effect);
                effect2IdMap.put(effect, id);
                effect2ImpactMap.put(effect, impact);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initInputEffectSet() {
        String inputEffect = AnnotationLevelFilterCommand.effectInput.replaceAll("( )+", "");

        if (inputEffect.isEmpty()) {
            return;
        }

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
        for (String effect : inputEffect.split(",")) {
            if (!effect2IdMap.containsKey(effect)) {
                LogManager.writeAndPrint("Invalid effect: " + effect);
                continue;
            }

            inputEffectSet.add(effect);
            inputIdSet.add(effect2IdMap.get(effect));
            inputImpactSet.add(effect2ImpactMap.get(effect));
        }
    }

    private static void initLowestImpact() {
        if (!inputEffectSet.isEmpty()) {
            isUsed = true;

            for (Impact impact : inputImpactSet) {
                if (lowestInputImpact.getValue() < impact.getValue()) {
                    lowestInputImpact = impact;
                }
            }
        } else {
            lowestInputImpact = Impact.MODIFIER;
        }
    }

    public static boolean isModifierEffectIncluded() {
        return lowestInputImpact == Impact.MODIFIER;
    }

    public static String getEffectIdList4SQL() {
        StringBuilder sb = new StringBuilder();

        boolean isFirst = true;

        for (int id : inputIdSet) {
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
