package function.external.kaviar;

/**
 *
 * @author nick
 */
public class KaviarManager {

    static final String snvTable = "kaviar.snv_maf_141127_2015_09_16";
    static final String indelTable = "kaviar.indel_maf_141127_2015_09_16";

    public static String getTitle() {
        String title = "Kaviar Allele Freq,"
                + "Kaviar Allele Count,"
                + "Kaviar Allele Number";

        return title;
    }

    public static String getSql(boolean isSnv, String chr,
            int pos, String ref, String alt) {
        String sql = "SELECT allele_frequency, allele_count, allele_number ";

        if (isSnv) {
            sql += "FROM " + snvTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND alt = '" + alt + "'";
        } else {
            sql += "FROM " + indelTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND ref = '" + ref + "' "
                    + "AND alt = '" + alt + "'";
        }

        return sql;
    }
}
