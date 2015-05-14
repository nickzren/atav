package atav.manager.data;

import atav.manager.utils.CommandValue;
import atav.manager.utils.ErrorManager;
import atav.manager.utils.FormatManager;
import atav.manager.utils.LogManager;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author nick
 */
public class FunctionManager {

    public static final String[] SNV_EFFECT_FULL_LIST = {"NON_SYNONYMOUS_CODING",
        "NON_SYNONYMOUS_START", "SPLICE_SITE_ACCEPTOR", "SPLICE_SITE_DONOR",
        "START_GAINED", "START_LOST", "STOP_GAINED", "STOP_LOST",
        "SYNONYMOUS_CODING", "SYNONYMOUS_START", "SYNONYMOUS_STOP"};
    public static final String[] HIT_TYPE_FULL_LIST = {"DOWNSTREAM", "EXON",
        "INTRAGENIC", "INTRON", "INTRON_EXON_BOUNDARY", "UPSTREAM",
        "UTR_3_PRIME", "UTR_5_PRIME", "INTERGENIC"};
    public static final String[] INDEL_EFFECT_FULL_LIST = {"CODON_CHANGE_PLUS_CODON_DELETION",
        "CODON_CHANGE_PLUS_CODON_INSERTION", "CODON_DELETION", "CODON_INSERTION",
        "EXON_DELETED", "FRAME_SHIFT", "SPLICE_SITE_ACCEPTOR", "SPLICE_SITE_DONOR", "START_LOST",
        "STOP_GAINED", "STOP_LOST", "UTR_3_DELETED", "UTR_5_DELETED"};
    public static final String[] RANDKING_FUNCTION__LIST = {
        "STOP_GAINED", "FRAME_SHIFT", "STOP_LOST", "START_LOST",
        "NON_SYNONYMOUS_CODING", "START_GAINED", "NON_SYNONYMOUS_START",
        "EXON_DELETED", "CODON_CHANGE_PLUS_CODON_DELETION", "CODON_CHANGE_PLUS_CODON_INSERTION",
        "CODON_DELETION", "CODON_INSERTION", "SPLICE_SITE_ACCEPTOR",
        "SPLICE_SITE_DONOR", "INTRON_EXON_BOUNDARY", "UTR_5_DELETED",
        "UTR_3_DELETED", "UTR_5_PRIME", "UTR_3_PRIME", "SYNONYMOUS_CODING",
        "SYNONYMOUS_START", "SYNONYMOUS_STOP", "EXON", "UPSTREAM", "DOWNSTREAM",
        "INTRON", "INTRAGENIC", "INTERGENIC"};
    public static String[] snvFunctionList = {"all"};
    public static String[] indelFunctionList = {"all"};
    private static HashMap<String, Integer> rankingFunctionMap = new HashMap<String, Integer>();
    private static HashSet<String> snvFunctionSet = new HashSet<String>();
    private static HashSet<String> indelFunctionSet = new HashSet<String>();
    private static String functions = "";

    private static HashSet<String> functionSet = new HashSet<String>();

    public static void init() {
        for (int i = 0; i < RANDKING_FUNCTION__LIST.length; i++) {
            rankingFunctionMap.put(RANDKING_FUNCTION__LIST[i], i);
        }

        for (String function : SNV_EFFECT_FULL_LIST) {
            snvFunctionSet.add(function);
        }

        for (String function : INDEL_EFFECT_FULL_LIST) {
            indelFunctionSet.add(function);
        }

        for (String function : HIT_TYPE_FULL_LIST) {
            snvFunctionSet.add(function);
            indelFunctionSet.add(function);
        }
    }

    public static void initFunctionList() {
        String str = CommandValue.functionInput.replaceAll("( )+", "");

        if (str.isEmpty()
                || str.endsWith("all")
                || str.endsWith("full-list")) {
            return;
        }

        File f = new File(str);

        if (f.isFile()) {
            initFromFile(f);
        } else {
            functions = str;
        }

        setFunctionList();
    }

    private static void initFromFile(File f) {
        String lineStr = "";
        int lineNum = 0;

        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                functions += lineStr + ",";
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in function file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    private static void setFunctionList() {
        functions = functions.replaceAll("( )+", "");
        String[] tempArray = functions.split(",");
        String snvFunctions = "";
        String indelFunctions = "";

        boolean isValid;
        for (String function : tempArray) {
            isValid = false;
            if (snvFunctionSet.contains(function)) {
                snvFunctions += function + ",";
                isValid = true;
            }

            if (indelFunctionSet.contains(function)) {
                indelFunctions += function + ",";
                isValid = true;
            }

            if (!isValid) {
                LogManager.writeAndPrint(function + " is not a valid function category.");
                continue;
            }

            functionSet.add(function);
        }

        snvFunctionList = snvFunctions.split(",");

        indelFunctionList = indelFunctions.split(",");
    }

    public static boolean isSNVAllFunction() {
        return snvFunctionList[0].equals("all") || snvFunctionList[0].equals("full-list");
    }

    public static boolean isINDELAllFunction() {
        return indelFunctionList[0].equals("all") || indelFunctionList[0].equals("full-list");
    }

    public static boolean isHitTypeContained(String[] functionList) {
        for (String function : functionList) {
            for (String hit_type : HIT_TYPE_FULL_LIST) {
                if (function.equals(hit_type)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isEffectTypeContained(String[] functionList, boolean isIndel) {
        for (String function : functionList) {
            if (isIndel) {
                for (String effect_type : INDEL_EFFECT_FULL_LIST) {
                    if (function.equals(effect_type)) {
                        return true;
                    }
                }
            } else {
                for (String effect_type : SNV_EFFECT_FULL_LIST) {
                    if (function.equals(effect_type)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isMoreDamage(String newFunction, String function) {
        Integer newF, oldF;

        newF = rankingFunctionMap.get(newFunction);
        oldF = rankingFunctionMap.get(function);

        if (newF == null || oldF == null) {
            return false;
        }

        return newF < oldF;
    }

    public static String getFunction(ResultSet rset, boolean isIndel) throws SQLException {
        String function = FormatManager.getFunction(rset.getString("hit_type"));

        if (function.equals("CODING_FUNCTION")
                || function.equals("TRANSCRIBE_FUNCTION")) {
            function = FormatManager.getFunction(rset.getString("effect_type"));

            if (!isFunctionContained(function, isIndel)) {
                function = "";
            }
        }

        return function;
    }

    public static boolean isFunctionContained(String function, boolean isIndel) {
        if (isIndel) {
            if (isINDELAllFunction()) {
                return true;
            }

            for (String effect_type : indelFunctionList) {
                if (function.equals(effect_type)) {
                    return true;
                }
            }
        } else {
            if (isSNVAllFunction()) {
                return true;
            }

            for (String effect_type : snvFunctionList) {
                if (function.equals(effect_type)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isValid(String function) {
        return functionSet.isEmpty()
                || functionSet.contains(function);
    }
}
