package function.external.exac;

import function.external.base.DataManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ExACManager {

    public static final String[] EXAC_POP = {"global", "afr", "amr", "eas", "sas", "fin", "nfe", "oth"};

    private static final String coverageTable = "exac.coverage_03";
    private static String variantTable = "exac.variant_r03_2015_09_16";
    private static String mnvTable = "exac.mnv_r03_2015_09_16";

    private static final String GENE_VARIANT_COUNT_PATH = "data/exac/ExAC.r0.3.damagingCounts.csv";
    private static final HashMap<String, String> geneVariantCountMap = new HashMap<>();
    private static String geneVariantCountHeader;
    private static StringJoiner NA = new StringJoiner(",");

    private static PreparedStatement preparedStatement4Variant;
    private static PreparedStatement preparedStatement4MNV;
    private static PreparedStatement preparedStatement4Region;
    private static PreparedStatement preparedStatement4Coverage;

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        for (String str : EXAC_POP) {
            sj.add("ExAC " + str + " af");
            sj.add("ExAC " + str + " gts");
        }

        sj.add("ExAC vqslod");
        sj.add("ExAC Mean Coverage");
        sj.add("ExAC Sample Covered 10x");

        return sj.toString();
    }

    public static String getGeneVariantCountHeader() {
        return geneVariantCountHeader;
    }

    public static void init() {
        if (ExACCommand.isIncludeCount) {
            initGeneVariantCountMap();
        }

        if (ExACCommand.isInclude) {
            initPreparedStatement();
        }
    }

    private static void initPreparedStatement() {
        preparedStatement4Variant = DBManager.initPreparedStatement(getSql4Variant(false));
        preparedStatement4MNV = DBManager.initPreparedStatement(getSql4Variant(true));
        preparedStatement4Region = DBManager.initPreparedStatement(getSql4Region());
        preparedStatement4Coverage = DBManager.initPreparedStatement(getSql4Cvg());
    }

    public static String getVersion() {
        return "ExAC: " + DataManager.getVersion(variantTable) + "\n";
    }

    public static String getSql4Cvg() {
        String sql = "SELECT mean_cvg, covered_10x FROM " + coverageTable + " WHERE chr=? AND pos=?";

        return sql;
    }

    public static String getSql4Region() {
        String select = "chr,pos,ref_allele,alt_allele,";

        for (String str : EXAC_POP) {
            select += str + "_af,"
                    + str + "_gts,";
        }

        select += "vqslod ";

        String sql = "SELECT " + select + "FROM " + variantTable + " "
                + "WHERE chr=? AND pos BETWEEN ? AND ?";

        return sql;
    }

    private static String getSql4Variant(boolean isMNV) {
        String select = "";

        for (String str : EXAC_POP) {
            select += str + "_af,"
                    + str + "_gts,";
        }

        select += "vqslod ";

        String table = isMNV ? mnvTable : variantTable;

        return "SELECT " + select + "FROM " + table + " "
                + "WHERE chr=? AND pos=? AND ref_allele=? AND alt_allele=?";
    }

    private static void initGeneVariantCountMap() {
        try {
            File f = new File(Data.ATAV_HOME + GENE_VARIANT_COUNT_PATH);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                int firstCommaIndex = lineStr.indexOf(",");
                String geneName = lineStr.substring(0, firstCommaIndex);
                String values = lineStr.substring(firstCommaIndex + 1);

                if (geneName.equals("Gene")) {
                    geneVariantCountHeader = values;

                    for (int i = 0; i < values.split(",").length; i++) {
                        NA.add(Data.STRING_NA);
                    }
                } else {
                    geneVariantCountMap.put(geneName, values);
                }
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getLine(String geneName) {
        String line = geneVariantCountMap.get(geneName);

        return line == null ? NA.toString() : line;
    }

    public static PreparedStatement getPreparedStatement4Variant(boolean isMNV) {
        return isMNV ? preparedStatement4MNV : preparedStatement4Variant;
    }

    public static PreparedStatement getPreparedStatement4Region() {
        return preparedStatement4Region;
    }

    public static PreparedStatement getPreparedStatement4Coverage() {
        return preparedStatement4Coverage;
    }
}
