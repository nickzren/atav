package function.variant.base;

import function.external.knownvar.ClinVar;
import function.external.knownvar.HGMD;
import function.external.knownvar.KnownVarManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.SampleManager;
import function.cohort.collapsing.CollapsingCommand;
import function.cohort.parent.ParentCommand;
import function.cohort.trio.TrioCommand;
import global.Data;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVRecord;
import utils.DBManager;
import utils.FormatManager;

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

    private static PreparedStatement preparedStatement4RS;
    private static PreparedStatement preparedStatement4MultiallelicVariant;
    private static PreparedStatement preparedStatement4MultiallelicVariant2;
    private static PreparedStatement preparedStatement4LOFTEEHCinCCDS;

    public static void init() throws FileNotFoundException, Exception, SQLException {
        if (TrioCommand.isList
                || ParentCommand.isList
                || CollapsingCommand.isCollapsingCompHet) {
            // disable process region as variant by varaint way
            maxIncludeNum = 0;
        }

        initPreparedStatement();

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
    }

    private static void initPreparedStatement() {
        if (!VariantLevelFilterCommand.includeRsNumber.isEmpty()) {
            String sql = "select chrom, POS from rs_number where rs_number=?";
            preparedStatement4RS = DBManager.initPreparedStatement(sql);
        }

        if (VariantLevelFilterCommand.isExcludeMultiallelicVariant) {
            String sql = "select pos from multiallelic_variant_site where chr=? and pos=?";
            preparedStatement4MultiallelicVariant = DBManager.initPreparedStatement(sql);
        }

        if (VariantLevelFilterCommand.isExcludeMultiallelicVariant2) {
            String sql = "select pos from multiallelic_variant_site_2 where chr=? and pos=?";
            preparedStatement4MultiallelicVariant2 = DBManager.initPreparedStatement(sql);
        }
        if (VariantLevelFilterCommand.isIncludeLOFTEE) {
            String sql = "select is_hc_in_ccds from loftee where chr=? and pos=? and ref=? and alt=?";
            preparedStatement4LOFTEEHCinCCDS = DBManager.initPreparedStatement(sql);
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
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                addVariantToList(lineStr, variantSet, isInclude);
            }

            br.close();
            fr.close();
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
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                addRsNumberToList(lineStr, rsNumberSet, isInclude);
            }

            br.close();
            fr.close();
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

        String[] values = str.split("-");
        String chr = values[0];
        String pos = values[1];
        RegionManager.checkChrValid(chr);

        if (!variantSet.contains(str)) {
            if (isInclude) {
                String varPos = chr + "-" + pos;

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
        preparedStatement4RS.setInt(1, rs);
        ResultSet rset = preparedStatement4RS.executeQuery();

        if (rset.next()) {
            String chr = rset.getString("chrom");
            int pos = rset.getInt("POS");
            rset.close();
            return chr + "-" + pos;
        }

        rset.close();

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

    public static boolean isValid(Variant var) throws Exception {
        if (VariantLevelFilterCommand.isExcludeSnv && var.isSnv()) {
            // exclude snv
            return false;
        } else if (VariantLevelFilterCommand.isExcludeIndel && var.isIndel()) {
            // exclude indel
            return false;
        } else if (VariantLevelFilterCommand.isExcludeMultiallelicVariant
                && isMultiallelicVariant(var.getChrStr(), var.getStartPosition())) {
            // exclude Multiallelic site > 1 variant
            return false;
        } else if (VariantLevelFilterCommand.isExcludeMultiallelicVariant2
                && isMultiallelicVariant2(var.getChrStr(), var.getStartPosition())) {
            // exclude Multiallelic site > 2 variants
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

    public static boolean isExcluded(String varId) {
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

    public static void initCaseVariantTable(String chr) {
        if (CohortLevelFilterCommand.isCaseOnlyValid2CreateTempTable()) {
            try {
                Statement stmt = DBManager.createStatementByConcurReadOnlyConn();

                String sqlQuery = "CREATE TEMPORARY TABLE tmp_case_variant_id_chr" + chr + " "
                        + "(case_variant_id INT NOT NULL,PRIMARY KEY (case_variant_id)) "
                        + "ENGINE=TokuDB "
                        + "SELECT DISTINCT variant_id AS case_variant_id FROM called_variant_chr" + chr + " "
                        + "WHERE sample_id IN (" + SampleManager.getCaseIDSJ().toString() + ")";

                stmt.executeUpdate(sqlQuery);
                stmt.closeOnCompletion();
            } catch (SQLException e) {
                ErrorManager.send(e);
            }
        }
    }

    public static void dropCaseVariantTable(String chr) {
        if (CohortLevelFilterCommand.isCaseOnlyValid2CreateTempTable()) {
            try {
                Statement stmt = DBManager.createStatementByConcurReadOnlyConn();

                String sqlQuery = "DROP TABLE tmp_case_variant_id_chr" + chr;

                stmt.executeUpdate(sqlQuery);
                stmt.closeOnCompletion();
            } catch (SQLException e) {
                ErrorManager.send(e);
            }
        }
    }

    // exclude Multiallelic site > 1 variant
    public static boolean isMultiallelicVariant(String chr, int pos) throws SQLException {
        preparedStatement4MultiallelicVariant.setString(1, chr);
        preparedStatement4MultiallelicVariant.setInt(2, pos);
        ResultSet rs = preparedStatement4MultiallelicVariant.executeQuery();

        return rs.next();
    }

    // exclude Multiallelic site > 2 variants
    public static boolean isMultiallelicVariant2(String chr, int pos) throws SQLException {
        preparedStatement4MultiallelicVariant2.setString(1, chr);
        preparedStatement4MultiallelicVariant2.setInt(2, pos);
        ResultSet rs = preparedStatement4MultiallelicVariant2.executeQuery();

        return rs.next();
    }

    // get LOFTEE HC in CCDS
    public static Boolean getLOFTEEHCinCCDS(String chr, int pos, String ref, String alt) throws SQLException {
        preparedStatement4LOFTEEHCinCCDS.setString(1, chr);
        preparedStatement4LOFTEEHCinCCDS.setInt(2, pos);
        preparedStatement4LOFTEEHCinCCDS.setString(3, ref);
        preparedStatement4LOFTEEHCinCCDS.setString(4, alt);
        ResultSet rs = preparedStatement4LOFTEEHCinCCDS.executeQuery();

        Boolean value = null;
        if (rs.next()) {
            value = rs.getBoolean("is_hc_in_ccds");
            if (rs.wasNull()) {
                value = null;
            }
        }
        rs.close();

        return value;
    }

    // get LOFTEE HC in CCDS
    public static Boolean getLOFTEEHCinCCDS(CSVRecord record) {
        return FormatManager.getBoolean(record, "LOFTEE-HC in CCDS");
    }

    public static boolean isUsed() {
        return isUsed;
    }

    public static boolean isVariantIdInputValid(String value) {
        if (Pattern.matches("^[atcgATCG0-9-,]+$", value)) {
            return true;
        } else {
            File file = new File(value);
            return file.exists();
        }
    }
}
