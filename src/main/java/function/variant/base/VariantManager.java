package function.variant.base;

import function.variant.base.RegionManager;
import function.annotation.base.GeneManager;
import function.variant.base.Region;
import function.variant.base.Variant;
import global.Data;
import global.SqlQuery;
import utils.CommandValue;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;
import utils.LogManager;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

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
        init(CommandValue.includeVariantInput, includeVariantSet, true);

        init(CommandValue.excludeVariantInput, excludeVariantSet, false);

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
        str = str.replaceAll("( )+", "");

        String rs = "";

        if (!variantSet.contains(str)) {
            if (str.startsWith("rs")) {
                rs = str;

                str = getVariantId(str);

                if (str.isEmpty()) {
                    return;
                }
            }

            if (isInclude) {
                add2IncludeIdList(str);
            }

            str = getPARVariantId(str);

            if (rs.isEmpty()) {
                variantSet.add(str);
            } else {
                variantSet.add(rs);
            }
        }
    }

    private static String getPARVariantId(String str) {
        if (!str.contains("X")
                || str.contains("XY")) {
            return str;
        }

        String chr = "";
        int start = 0, end = 0;

        if (str.contains("_")) {
            String[] temp = str.split("_");

            chr = temp[0];
            start = Integer.valueOf(temp[1]);
            end = start;

            if (temp.length > 2
                    && FormatManager.isInteger(temp[2])) {
                end = Integer.valueOf(temp[2]);
            }
        } else if (str.contains("-")) {
            String[] temp = str.split("-");

            chr = temp[0];
            start = Integer.valueOf(temp[1]);
            end = start;
        }

        Region region = new Region(chr, start, end);

        if (region.isInsideXPseudoautosomalRegions()) {
            if (str.contains("_")) {
                return "XY" + str.substring(str.indexOf("_"));
            } else {
                return "XY" + str.substring(str.indexOf("-"));
            }
        }

        return str;
    }

    private static void add2IncludeIdList(String str) {
        if (str.startsWith("chr")) {
            str = str.substring(3, str.length());
        }

        String chr = "";

        if (str.contains("_")) {
            chr = str.split("_")[0];

            if (str.split("_").length == 2) { // snv position
                str += "_snv";
            }
        } else if (str.contains("-")) {
            chr = str.split("-")[0];
        }

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

    private static boolean isIncluded(Variant var) {
        if (includeVariantSet.isEmpty()) {
            return true;
        } else {
            if (includeVariantSet.contains(var.getVariantIdStr())
                    || includeVariantSet.contains(var.getNewVariantIdStr())
                    || includeVariantSet.contains(var.getRsNumber())
                    || includeVariantSet.contains(var.getPositionStr())) {
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
                    || excludeVariantSet.contains(var.getNewVariantIdStr())
                    || excludeVariantSet.contains(var.getRsNumber())
                    || excludeVariantSet.contains(var.getPositionStr());
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

    private static String getVariantId(String rs) throws SQLException {
        String sqlCode = SqlQuery.SNV_ID.replace("_RS_", rs);

        ResultSet rset = DBManager.executeQuery(sqlCode);

        String chr = "";
        int start = 0, end = 0;
        if (rset.next()) {
            chr = rset.getString("name");
            start = rset.getInt("seq_region_pos");
            return chr + "_" + start + "_snv";
        } else {
            sqlCode = SqlQuery.INDEL_ID.replace("_RS_", rs);

            rset = DBManager.executeQuery(sqlCode);
            if (rset.next()) {
                chr = rset.getString("name");
                start = rset.getInt("seq_region_pos");
                end = start + rset.getInt("length") - 1;
                return chr + "_" + start + "_" + end;
            }
        }

        return "";
    }

    public static ArrayList<String> getIncludeVariantList() {
        return includeIdList;
    }
}
