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
    private static HashSet<String> includeRsNumberSet = new HashSet<>();
    private static HashSet<String> excludeVariantSet = new HashSet<>();
    private static ArrayList<String> includeVariantPosList = new ArrayList<>();
    private static ArrayList<String> includeVariantTypeList = new ArrayList<>();
    private static ArrayList<String> includeChrList = new ArrayList<>();

    private static final int maxIncludeNum = 10000000;

    public static void init() throws FileNotFoundException, Exception, SQLException {
        initByVariantId(VariantLevelFilterCommand.includeVariantId, includeVariantSet, true);

        initByRsNumber(VariantLevelFilterCommand.includeRsNumber, includeRsNumberSet, true);

        initByVariantId(VariantLevelFilterCommand.excludeVariantId, excludeVariantSet, false);

        if (VariantLevelFilterCommand.isExcludeArtifacts) {
            initByVariantId(ARTIFACTS_Variant_PATH, excludeVariantSet, false);
        }

        if (!VariantLevelFilterCommand.includeVariantId.isEmpty()
                || !VariantLevelFilterCommand.includeRsNumber.isEmpty()) {
            resetRegionList();
        }
    }

    public static void initByVariantId(String input, HashSet<String> variantSet, boolean isInclude)
            throws Exception {
        if (input.isEmpty()) {
            return;
        }

        File f = new File(input);

        if (f.isFile()) {
            initFromVariantFile(f, variantSet, isInclude);
        } else {
            String[] list = input.split(",");

            for (String str : list) {
                addVariantToList(str, variantSet, isInclude);
            }
        }
    }

    public static void initByRsNumber(String input, HashSet<String> rsNumberSet, boolean isInclude)
            throws Exception {
        if (input.isEmpty()) {
            return;
        }

        File f = new File(input);

        if (f.isFile()) {
            initFromRsNumberFile(f, rsNumberSet, isInclude);
        } else {
            String[] list = input.split(",");

            for (String str : list) {
                addRsNumberToList(str, rsNumberSet, isInclude);
            }
        }
    }

    private static void initFromVariantFile(File f,
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

    private static void initFromRsNumberFile(File f,
            HashSet<String> rsNumberSet, boolean isInclude) {
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

                addRsNumberToList(lineStr, rsNumberSet, isInclude);
            }
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in rs number file: " + lineStr);

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
        } else if (str.startsWith("rs")) {
            ErrorManager.print("warning: rs number is no longer support in --variant option, "
                    + "please use --rs-number instead.");
        }

        str = str.replaceAll("( )+", "");

        if (str.startsWith("chr")) {
            str = str.substring(3, str.length());
        }

        if (!variantSet.contains(str)) {
            if (isInclude) {
                String[] values = str.split("-");
                String varPos = values[0] + "-" + values[1];
                if (values[2].length() == 1
                        && values[3].length() == 1) {
                    varPos += "-snv";
                } else {
                    varPos += "-indel";
                }

                add2IncludeVariantPosSet(varPos);
            }

            str = getPARVariantId(str);

            variantSet.add(str);
        }
    }

    private static void addRsNumberToList(String str, HashSet<String> rsNumberSet,
            boolean isInclude) throws SQLException {
        str = str.replaceAll("( )+", "");

        if (!rsNumberSet.contains(str)) {
            String varPos = VariantManager.getVariantPositionByRS(str);

            if (!varPos.isEmpty()) {
                if (isInclude) {
                    add2IncludeVariantPosSet(varPos);
                }

                rsNumberSet.add(str);
            }
        }
    }

    private static String getVariantPositionByRS(String rs) throws SQLException {
        String varPos = getVariantPositionByRS(rs, "snv");

        if (varPos.isEmpty()) {
            varPos = getVariantPositionByRS(rs, "indel");
        }

        return varPos;
    }

    private static String getVariantPositionByRS(String rs, String type) throws SQLException {
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

    private static void add2IncludeVariantPosSet(String str) {
        String chr = str.split("-")[0];

        if (!includeChrList.contains(chr)) {
            includeChrList.add(chr);
        }

        if (!includeVariantPosList.contains(str)) {
            includeVariantPosList.add(str);
        }
    }

    private static void resetRegionList() throws SQLException {
        if (!RegionManager.isUsed()
                && !GeneManager.isUsed()) {
            RegionManager.clear();

            if (includeVariantSet.size() <= maxIncludeNum) {
                for (String varPos : includeVariantPosList) {
                    RegionManager.addRegionByVariantPos(varPos);
                }
            } else {
                includeVariantTypeList.clear();
                RegionManager.initChrRegionList(includeChrList.toArray(new String[includeChrList.size()]));
                RegionManager.sortRegionList();
            }
        }
    }

    public static boolean isValid(Variant var) {
        return (isVariantIdIncluded(var.getVariantIdStr())
                && isRsNumberIncluded(var.getRsNumber()))
                && !isExcluded(var.getVariantIdStr());
    }

    public static boolean isVariantIdIncluded(String varId) {
        if (includeVariantSet.isEmpty()
                && VariantLevelFilterCommand.includeVariantId.isEmpty()) {
            // only when --variant option not used, return true
            return true;
        } else {
            if (includeVariantSet.contains(varId)) {
                includeVariantSet.remove(varId);
                return true;
            }

            return false;
        }
    }

    private static boolean isRsNumberIncluded(String rs) {
        if (includeRsNumberSet.isEmpty()) {
            // only when --rs-number option not used, return true
            return true;
        } else {
            return includeRsNumberSet.contains(rs);
        }
    }

    private static boolean isExcluded(String varId) {
        return excludeVariantSet.contains(varId);
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
        return includeVariantPosList;
    }

    private static void clearIncludeVarSet() {
        includeVariantSet.clear();
        includeRsNumberSet.clear();
        includeVariantPosList.clear();
        includeVariantTypeList.clear();
        includeChrList.clear();
    }

    public static boolean isAnnoDBVar(boolean isSnv, String chr,
            int pos, String ref, String alt) throws SQLException {
        String sql;
        int regionId = RegionManager.getIdByChr(chr);

        if (isSnv) {
            sql = "SELECT snv_id From snv "
                    + "WHERE seq_region_id=" + regionId + " "
                    + "AND seq_region_pos=" + pos + " "
                    + "AND allele='" + alt + "'";
        } else {
            sql = "SELECT indel_id From indel "
                    + "WHERE seq_region_id=" + regionId + " "
                    + "AND seq_region_pos=" + pos + " "
                    + "AND ref_allele='" + ref + "'"
                    + "AND allele='" + alt + "'";
        }

        ResultSet rs = DBManager.executeQuery(sql);

        return rs.next();
    }
}
