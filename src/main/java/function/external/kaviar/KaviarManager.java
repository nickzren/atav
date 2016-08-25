package function.external.kaviar;

import function.external.base.DataManager;
import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class KaviarManager {

    static final String snvTable = "kaviar.snv_maf_160113";
    static final String indelTable = "kaviar.indel_maf_160113";

    public static String getTitle() {
        String title = "";

        if (KaviarCommand.isIncludeKaviar) {
            title = "Kaviar Maf,"
                    + "Kaviar Allele Count,"
                    + "Kaviar Allele Number,";
        }

        return title;
    }

    public static String getVersion() {
        if (KaviarCommand.isIncludeKaviar) {
            return "Kaviar: " + DataManager.getVersion(snvTable) + "\n";
        } else {
            return "";
        }
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

    public static String getSql(boolean isIndel, Region region) {
        String table = snvTable;

        if (isIndel) {
            table = indelTable;
        }

        String sql = "SELECT * FROM " + table + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }
}
