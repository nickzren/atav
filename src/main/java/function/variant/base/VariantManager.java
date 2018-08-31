package function.variant.base;

import function.external.knownvar.ClinVar;
import function.external.knownvar.HGMD;
import function.external.knownvar.KnownVarManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import function.genotype.parent.ParentCommand;
import function.genotype.trio.TrioCommand;
import function.genotype.vargeno.VarGenoCommand;
import global.Data;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.lang.StringEscapeUtils;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class VariantManager {

    private static final String ARTIFACTS_Variant_PATH = Data.ATAV_HOME + "data/artifacts_variant.txt";
    public static final String[] VARIANT_TYPE = {"snv", "indel"};

    private static HashSet<String> includeVariantSet = new HashSet<>();
    private static HashSet<Integer> includeRsNumberSet = new HashSet<>();
    private static HashSet<String> excludeVariantSet = new HashSet<>();
    private static ArrayList<String> includeVariantPosList = new ArrayList<>();
    private static ArrayList<String> includeChrList = new ArrayList<>();

    private static boolean isUsed = false;

    private static int maxIncludeNum = 200000;

    public static void init() throws FileNotFoundException, Exception, SQLException {
        if (TrioCommand.isListTrio
                || ParentCommand.isListParentCompHet) {
            // disable process region as variant by varaint way
            maxIncludeNum = 0;
        }

        initByVariantId(VariantLevelFilterCommand.includeVariantId, includeVariantSet, true);

        initByRsNumber(VariantLevelFilterCommand.includeRsNumber, includeRsNumberSet, true);

        initByVariantId(VariantLevelFilterCommand.excludeVariantId, excludeVariantSet, false);

        if (VariantLevelFilterCommand.isExcludeArtifacts) {
            initByVariantId(ARTIFACTS_Variant_PATH, excludeVariantSet, false);
        }

        if (!VariantLevelFilterCommand.includeVariantId.isEmpty()
                || !VariantLevelFilterCommand.includeRsNumber.isEmpty()) {
            isUsed = true;

            resetRegionList();
        }
        
        initCaseVariantTable();
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

    public static void initByRsNumber(String input, HashSet<Integer> rsNumberSet, boolean isInclude)
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
            HashSet<Integer> rsNumberSet, boolean isInclude) {
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

//        resetRegionList();
    }

    private static void addVariantToList(String str, HashSet<String> variantSet,
            boolean isInclude) throws SQLException {
        if (str.startsWith("rs")) {
            ErrorManager.print("warning: rs number is no longer support in --variant option, "
                    + "please use --rs-number instead.", ErrorManager.INPUT_PARSING);
        }

        str = str.replaceAll("( )+", "").replace("XY", "X");

        if (str.startsWith("chr")) {
            str = str.substring(3, str.length());
        }

        if (!variantSet.contains(str)) {
            if (isInclude) {
                String[] values = str.split("-");
                String varPos = values[0] + "-" + values[1];

                add2IncludeVariantPosSet(varPos);
            }

            variantSet.add(str);
        }
    }

    private static void addRsNumberToList(String str, HashSet<Integer> rsNumberSet,
            boolean isInclude) throws SQLException {
        int value = Integer.valueOf(str.replaceAll("( )+", "").replace("rs", ""));

        if (!rsNumberSet.contains(value)) {
            String varPos = VariantManager.getVariantPositionByRS(value);

            if (!varPos.isEmpty()) {
                if (isInclude) {
                    add2IncludeVariantPosSet(varPos);
                }

                rsNumberSet.add(value);
            }
        }
    }

    private static String getVariantPositionByRS(int rs) throws SQLException {
        String sql = "select chrom, POS from rs_number "
                + "where rs_number = " + rs;

        ResultSet rset = DBManager.executeQuery(sql);

        if (rset.next()) {
            String chr = rset.getString("chrom");
            int pos = rset.getInt("POS");
            return chr + "-" + pos;
        }

        return "";
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
        RegionManager.clear();

        if (includeVariantSet.size() <= maxIncludeNum) {
            for (String varPos : includeVariantPosList) {
                RegionManager.addRegionByVariantPos(varPos);
            }
        } else {
            RegionManager.initChrRegionList(includeChrList.toArray(new String[includeChrList.size()]));
            RegionManager.sortRegionList();
        }
    }

    public static boolean isValid(Variant var) {
        if (VariantLevelFilterCommand.isExcludeSnv && var.isSnv()) {
            return false;
        } else if (VariantLevelFilterCommand.isExcludeIndel && var.isIndel()) {
            return false;
        }

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
                return true;
            }

            return false;
        }
    }

    private static boolean isRsNumberIncluded(int rs) {
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

    public static HashSet<String> getIncludeVariantSet() {
        return includeVariantSet;
    }

    private static void clearIncludeVarSet() {
        includeVariantSet.clear();
        includeRsNumberSet.clear();
        includeVariantPosList.clear();
        includeChrList.clear();
    }

    private static void initCaseVariantTable() {
        if (GenotypeLevelFilterCommand.isCaseOnly) {
            try {
                Statement stmt = DBManager.createStatementByReadOnlyConn();

                for (String chr : RegionManager.getChrList()) {
                    String sqlQuery = "CREATE TEMPORARY TABLE tmp_case_variant_id_chr" + chr + " "
                            + "(case_variant_id INT NOT NULL,PRIMARY KEY (case_variant_id)) "
                            + "ENGINE=TokuDB "
                            + "SELECT DISTINCT variant_id AS case_variant_id FROM called_variant_chr" + chr + " "
                            + "WHERE sample_id IN (" + SampleManager.getCaseIDSJ().toString() + ")";

                    stmt.executeUpdate(sqlQuery);
                    stmt.closeOnCompletion();
                }
            } catch (Exception e) {
                ErrorManager.send(e);
            }
        }
    }

    public static boolean isUsed() {
        return isUsed;
    }
}
