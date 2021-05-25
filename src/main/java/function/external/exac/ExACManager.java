package function.external.exac;

import function.external.base.DataManager;
import java.sql.PreparedStatement;
import java.util.StringJoiner;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class ExACManager {

    public static final String[] POP = {"global", "afr", "amr", "eas", "sas", "fin", "nfe", "oth"};

    private static final String coverageTable = "exac.coverage_03";
    private static String variantTable = "exac.variant_r03_2015_09_16";
    private static String mnvTable = "exac.mnv_r03_2015_09_16";

    private static PreparedStatement preparedStatement4Variant;
    private static PreparedStatement preparedStatement4MNV;
    private static PreparedStatement preparedStatement4Region;
    private static PreparedStatement preparedStatement4Coverage;

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        for (String str : POP) {
            sj.add("ExAC " + str + " af");
            sj.add("ExAC " + str + " gts");
        }

        sj.add("ExAC vqslod");
        sj.add("ExAC Mean Coverage");
        sj.add("ExAC Sample Covered 10x");

        return sj.toString();
    }

    public static void init() {
        if (ExACCommand.getInstance().isInclude) {
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

        for (String str : POP) {
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

        for (String str : POP) {
            select += str + "_af,"
                    + str + "_gts,";
        }

        select += "vqslod ";

        String table = isMNV ? mnvTable : variantTable;

        return "SELECT " + select + "FROM " + table + " "
                + "WHERE chr=? AND pos=? AND ref_allele=? AND alt_allele=?";
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
