package function.external.exac;

/**
 *
 * @author nick
 */
public class ExacManager {

    public static final String[] EXAC_POP = {"global", "afr", "amr", "eas", "sas", "fin", "nfe", "oth"};
    public static final String[] EXAC_SUBSET = {"nonpsych", "nonTCGA"};

    static final String coverageTable = "exac.coverage_03";
    static String snvTable = "exac.snv_maf_r03_2015_09_16";
    static String indelTable = "exac.indel_maf_r03_2015_09_16";

    public static void resetTables() {
        if (ExacCommand.exacSubset.equalsIgnoreCase("nonpsyc")) {
            snvTable = "exac.snv_maf_r03_nonpsych";
            indelTable = "exac.indel_maf_r03_nonpsych";
        } else if (ExacCommand.exacSubset.equalsIgnoreCase("nonTCGA")) {
            snvTable = "exac.snv_maf_r03_nonTCGA";
            indelTable = "exac.indel_maf_r03_nonTCGA";
        }
    }

    public static String getTitle() {
        String title = "";

        if (ExacCommand.isIncludeExac) {
            for (String str : EXAC_POP) {
                title += "ExAC " + str + " maf,"
                        + "ExAC " + str + " gts,";
            }

            title += "ExAC vqslod,"
                    + "ExAC Mean Coverage,"
                    + "ExAC Sample Covered 10x,";
        }

        return title;
    }

    public static String getSql4Cvg(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM " + coverageTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    public static String getSql4Maf(boolean isSnv, String chr,
            int pos, String ref, String alt) {
        String result = "";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";

        String sql = "SELECT " + result;

        if (isSnv) {
            sql += "FROM " + snvTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND alt_allele = '" + alt + "'";
        } else {
            sql += "FROM " + indelTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND ref_allele = '" + ref + "' "
                    + "AND alt_allele = '" + alt + "'";
        }

        return sql;
    }
}
