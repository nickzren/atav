package function.variant.base;

import function.annotation.base.GeneManager;
import global.Data;
import global.SqlQuery;
import utils.CommandValue;
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

    private static HashSet<String> includeVariantSet = new HashSet<String>();
    private static HashSet<String> excludeVariantSet = new HashSet<String>();
    private static ArrayList<String> includeIdList = new ArrayList<String>();
    private static ArrayList<String> includeVariantTypeList = new ArrayList<String>();
    private static ArrayList<String> includeChrList = new ArrayList<String>();

    private static HashSet<Integer> outputVariantIdSet = new HashSet<Integer>();

    private static final int maxIncludeNum = 10000000;

    public static void init() throws FileNotFoundException, Exception, SQLException {
        init(CommandValue.includeVariantId, includeVariantSet, true);

        init(CommandValue.excludeVariantId, excludeVariantSet, false);

        if (CommandValue.isExcludeArtifacts) {
            init(Data.ARTIFACTS_Variant_PATH, excludeVariantSet, false);
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
            initRegionList();
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
        String varPos = getVariantPosition(rs, SqlQuery.SNV_ID, "snv");

        if (varPos.isEmpty()) {
            varPos = getVariantPosition(rs, SqlQuery.INDEL_ID, "indel");
        }

        return varPos;
    }

    private static String getVariantPosition(String rs, String sql, String type) throws SQLException {
        sql = sql.replace("_RS_", rs);

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

    private static void initRegionList() throws SQLException {
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
        if (isIncluded(var) && !isExcluded(var)) {
            return true;
        }

        return false;
    }

    public static boolean isIncluded(Variant var) {
        if (includeVariantSet.isEmpty()) {
            return true;
        } else {
            if (includeVariantSet.contains(var.getVariantIdStr())
                    || includeVariantSet.contains(var.getRsNumber())) {
                if (outputVariantIdSet.contains(var.getVariantId())) {
                    return false;
                } else {
                    outputVariantIdSet.add(var.getVariantId());
                    return true;
                }
            } else {
                return false;
            }
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
            if (type.equals(Data.VARIANT_TYPE[0])) // snv
            {
                if (CommandValue.isExcludeSnv) {
                    return false;
                }
            } else // indel
            {
                if (CommandValue.isExcludeIndel) {
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
}
