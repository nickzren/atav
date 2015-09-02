package function.external.exac;

import global.Data;

/**
 *
 * @author nick
 */
public class ExacManager {

    public static String getTitle() {
        String title = "";

        for (String str : Data.EXAC_POP) {
            title += "ExAC " + str + " maf,"
                    + "ExAC " + str + " gts,";
        }

        title += "ExAC vqslod,"
                + "ExAC Mean Coverage,"
                + "ExAC Sample Covered 10x,";

        return title;
    }

    public static String getSql4Cvg(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM exac.coverage_03 "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    public static String getSql4Maf(boolean isSnv, String chr, 
            int pos, String ref, String alt) {
        String result = "";

        for (String str : Data.EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";

        String sql = "SELECT " + result;

        if (isSnv) {
            sql += "FROM exac.snv_maf_r03 "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND alt_allele = '" + alt + "'";
        } else {
            sql += "FROM exac.indel_maf_r03 "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND ref_allele = '" + ref + "' "
                    + "AND alt_allele = '" + alt + "'";
        }

        return sql;
    }
}
