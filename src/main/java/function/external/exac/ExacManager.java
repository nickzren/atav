package function.external.exac;

import function.external.base.DataManager;
import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class ExacManager {

    public static final String[] EXAC_POP = {"global", "afr", "amr", "asj", "eas", "sas", "fin", "nfe", "oth"};

    static final String coverageTable = "exac.coverage_v2_2017_03_01";
    static String table = "exac.variant_v2_2017_03_01";

    public static void init() {
    }

    public static String getTitle() {
        String title = "";

        if (ExacCommand.isIncludeExac) {
            for (String str : EXAC_POP) {
                title += "ExAC " + str + " maf,"
                        + "ExAC " + str + " gts,";
            }

            title += "ExAC filter,"
                    + "ExAC AB MEDIAN,"
                    + "ExAC GQ MEDIAN,"
                    + "ExAC AS RF,"
                    + "ExAC Mean Coverage,"
                    + "ExAC Sample Covered 10x,";
        }

        return title;
    }

    public static String getVersion() {
        if (ExacCommand.isIncludeExac) {
            return "ExAC: " + DataManager.getVersion(table) + "\n";
        } else {
            return "";
        }
    }

    public static String getSql4Cvg(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM " + coverageTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    public static String getSqlByRegion(Region region) {
        String result = "chr,pos,ref,alt,";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF ";

        String sql = "SELECT " + result
                + "FROM " + table + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }

    public static String getSqlByVariant(String chr,
            int pos, String ref, String alt) {
        String result = "";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF ";

        return "SELECT " + result
                + "FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";
    }
}
