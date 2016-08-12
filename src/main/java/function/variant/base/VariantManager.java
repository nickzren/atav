package function.variant.base;

import function.annotation.base.GeneManager;
import function.external.knownvar.ClinVar;
import function.external.knownvar.HGMD;
import function.external.knownvar.KnownVarManager;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class VariantManager {

    private static final String ARTIFACTS_Variant_PATH = "data/artifacts_variant.txt";
    public static final String[] VARIANT_TYPE = {"snv", "indel"};

    private static HashSet<String> includeVariantSet = new HashSet<>();
    private static HashSet<String> excludeVariantSet = new HashSet<>();
    private static ArrayList<String> includeIdList = new ArrayList<>();
    private static ArrayList<String> includeVariantTypeList = new ArrayList<>();
    private static ArrayList<String> includeChrList = new ArrayList<>();

    // used this id set to avoid output duplicate variants
    private static HashSet<Integer> outputVariantIdSet = new HashSet<>();

    private static final int maxIncludeNum = 10000000;

    public static void init() throws FileNotFoundException, Exception, SQLException {
        init(VariantLevelFilterCommand.includeVariantId, includeVariantSet, true);

        init(VariantLevelFilterCommand.excludeVariantId, excludeVariantSet, false);

        if (VariantLevelFilterCommand.isExcludeArtifacts) {
            init(ARTIFACTS_Variant_PATH, excludeVariantSet, false);
        }
    }

    public static void init(String input, HashSet<String> variantSet, boolean isInclude)
            throws FileNotFoundException, Exception, SQLException {
        if (input.isEmpty()) {
            return;
        }

        File f = new File(input);

        if (f.isFile()) {
            initFromFile(f, variantSet, isInclude);
        } else {
            String[] list = input.split(",");

            for (String str : list) {
                addVariantToList(str, variantSet, isInclude);
            }
        }

        if (isInclude) {
            resetRegionList();
        }
    }

    private static void initFromFile(File f,
            HashSet<String> variantSet, boolean isInclude) {
        String lineStr = "";
        int lineNum = 0;

        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                addVariantToList(lineStr, variantSet, isInclude);
            }
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in variant file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    public static void reset2KnownVarSet() throws SQLException {
        clearIncludeVarSet();

        // init ClinVar variants set
        for (ClinVar clinvar : KnownVarManager.getClinVarMultiMap().values()) {
            addVariantToList(clinvar.getVariantId(), includeVariantSet, true);
        }

        // init HGMD variants set
        for (HGMD hgmd : KnownVarManager.getHGMDMultiMap().values()) {
            addVariantToList(hgmd.getVariantId(), includeVariantSet, true);
        }

        resetRegionList();
    }

    private static void addVariantToList(String str, HashSet<String> variantSet,
            boolean isInclude) throws SQLException {
        if (str.contains("_")) {
            ErrorManager.print("warning: old variant id format is no longer support.");
        }

        str = str.replaceAll("( )+", "");

        if (!variantSet.contains(str)) {
            if (str.startsWith("rs")) { // add by rs#
                String varPos = getVariantPosition(str);

                if (!varPos.isEmpty()) {
                    if (isInclude) {
                        add2IncludeIdList(varPos);
                    }

                    variantSet.add(str);
                }
            } else { // add by variand id
                if (isInclude) {
                    add2IncludeIdList(str);
                }

                str = getPARVariantId(str);

                variantSet.add(str);
            }
        }
    }

    private static String getVariantPosition(String rs) throws SQLException {
        String varPos = getVariantPosition(rs, "snv");

        if (varPos.isEmpty()) {
            varPos = getVariantPosition(rs, "indel");
        }

        return varPos;
    }

    private static String getVariantPosition(String rs, String type) throws SQLException {
        String sql = "select name, seq_region_pos from " + type + " v, seq_region s "
                + "where rs_number = '" + rs + "' and "
                + "coord_system_id = 2 and "
                + "v.seq_region_id = s.seq_region_id";

        ResultSet rset = DBManager.executeQuery(sql);

        String chr = "";
        int pos = 0;
        if (rset.next()) {
            chr = rset.getString("name");
            pos = rset.getInt("seq_region_pos");
            return chr + "-" + pos + "-" + type;
        }

        return "";
    }

    private static String getPARVariantId(String str) {
        if (!str.contains("X")
                || str.contains("XY")) {
            return str;
        }

        String[] temp = str.split("-");

        String chr = temp[0];
        int pos = Integer.valueOf(temp[1]);

        Region region = new Region(chr, pos, pos);

        if (region.isInsideXPseudoautosomalRegions()) {
            return "XY" + str.substring(str.indexOf("-"));
        }

        return str;
    }

    private static void add2IncludeIdList(String str) {
        if (str.startsWith("chr")) {
            str = str.substring(3, str.length());
        }

        String chr = str.split("-")[0];

        if (!includeChrList.contains(chr)) {
            includeChrList.add(chr);
        }

        includeIdList.add(str);
    }

    private static void resetRegionList() throws SQLException {
        if (!RegionManager.isUsed()
                && !GeneManager.isUsed()) {
            RegionManager.clear();

            if (includeVariantSet.size() <= maxIncludeNum) {
                for (String id : includeIdList) {
                    RegionManager.addRegionByVariantId(id);
                }
            } else {
                includeVariantTypeList.clear();
                RegionManager.initChrRegionList(includeChrList.toArray(new String[includeChrList.size()]));
                RegionManager.sortRegionList();
            }
        }
    }

    public static boolean isValid(Variant var) {
        return isIncluded(var) && !isExcluded(var);
    }

    public static boolean isIncluded(Variant var) {
        if (includeVariantSet.isEmpty()) {
            return true;
        } else {
            return includeVariantSet.contains(var.getVariantIdStr())
                    || includeVariantSet.contains(var.getRsNumber());
        }
    }

    private static boolean isExcluded(Variant var) {
        if (excludeVariantSet.isEmpty()) {
            return false;
        } else {
            return excludeVariantSet.contains(var.getVariantIdStr())
                    || excludeVariantSet.contains(var.getRsNumber());
        }
    }

    public static boolean isVariantTypeValid(int index, String type) {
        boolean check = false;

        try {
            if (type.equals(VARIANT_TYPE[0])) // snv
            {
                if (VariantLevelFilterCommand.isExcludeSnv) {
                    return false;
                }
            } else // indel
            {
                if (VariantLevelFilterCommand.isExcludeIndel) {
                    return false;
                }
            }

            if (includeVariantTypeList.isEmpty()) {
                return true;
            }

            check = includeVariantTypeList.get(index).equalsIgnoreCase(type);
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return check;
    }

    public static void addType(String type) {
        includeVariantTypeList.add(type);
    }

    public static ArrayList<String> getIncludeVariantList() {
        return includeIdList;
    }

    public static boolean isVariantOutput(int id) {
        if (includeIdList.isEmpty()) { // only apply when used --variant option
            return false;
        }

        if (outputVariantIdSet.contains(id)) {
            return true;
        } else {
            outputVariantIdSet.add(id);
            return false;
        }
    }

    private static void clearIncludeVarSet() {
        includeVariantSet.clear();
        includeIdList.clear();
        includeVariantTypeList.clear();
        includeChrList.clear();
    }
}
