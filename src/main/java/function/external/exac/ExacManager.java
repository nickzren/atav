package function.external.exac;

import function.external.base.DataManager;
import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class ExacManager {

    public static final String[] EXAC_POP = {"global", "afr", "amr", "eas", "sas", "fin", "nfe", "oth"};

    static final String coverageTable = "exac.coverage_03";
    static String variantTable = "exac.variant_r03_2015_09_16";

    public static String getTitle() {
        String title = "";

        if (ExacCommand.isIncludeExac) {
            for (String str : EXAC_POP) {
                title += "ExAC " + str + " af,"
                        + "ExAC " + str + " gts,";
            }

            title += "ExAC vqslod,"
                    + "ExAC Mean Coverage,"
                    + "ExAC Sample Covered 10x,";
        }

        return title;
    }

    public static void init() {
    }

    public static String getVersion() {
        if (ExacCommand.isIncludeExac) {
            return "ExAC: " + DataManager.getVersion(variantTable) + "\n";
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
        String result = "chr,pos,ref_allele,alt_allele,";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";

        String sql = "SELECT " + result
                + "FROM " + variantTable + " "
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

        result += "vqslod ";

        return "SELECT " + result
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";
    }
}
