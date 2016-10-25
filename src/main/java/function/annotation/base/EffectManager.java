package function.annotation.base;

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

    // user input values
    private static HashSet<String> inputEffectSet = new HashSet<>();
    private static HashSet<Integer> inputIdSet = new HashSet<>();

    private static boolean isUsed = false;

    public static void init() {
        initDefaultEffectSet();

        initInputEffectSet();
    }

    private static void initDefaultEffectSet() {
        try {
            String sql = "SELECT id,effect FROM effect_ranking";

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String effect = rs.getString("effect");

                id2EffectMap.put(id, effect);
                effect2IdMap.put(effect, id);
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
            String effectFilePath = inputEffect;

            try (Stream<String> stream = Files.lines(Paths.get(effectFilePath))) {
                inputEffect = stream.map(line -> line.replaceAll("( )+", ""))
                        .collect(Collectors.joining(","));
            } catch (IOException e) {
                ErrorManager.send(e);
            }
        }

        for (String effect : inputEffect.split(",")) {
            if (!effect2IdMap.containsKey(effect)) {
                LogManager.writeAndPrint("Invalid effect: " + effect);
                continue;
            }

            inputEffectSet.add(effect);
            inputIdSet.add(effect2IdMap.get(effect));
        }

        if (inputEffectSet.size() > 0) {
            isUsed = true;
        }
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

    public static String getEffectById(int id){
        return id2EffectMap.get(id);
    }

    public static boolean isUsed() {
        return isUsed;
    }
}
