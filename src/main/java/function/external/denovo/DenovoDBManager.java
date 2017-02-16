package function.external.denovo;

import function.external.base.DataManager;
import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class DenovoDBManager {

    static final String table = "knownvar.denovodb_2017_02_06";

    public static String getTitle() {
        String title = "";

        if (DenovoDBCommand.isIncludeDenovoDB) {
            title = "DenovoDB Phenotype,"
                    + "DenovoDB PubmedID,";
        }

        return title;
    }

    public static String getVersion() {
        if (DenovoDBCommand.isIncludeDenovoDB) {
            return "DenovoDB: " + DataManager.getVersion(table) + "\n";
        } else {
            return "";
        }
    }

    public static String getSql(String chr, int pos, String ref, String alt) {
        return "SELECT Phenotype, PubmedID"
                + " FROM " + table + " "
                + " WHERE chr = '" + chr + "'"
                + " AND pos = " + pos
                + " AND ref = '" + ref + "'"
                + " AND alt = '" + alt + "'";
    }

    public static String getSql(Region region) {
        return "SELECT * FROM " + table + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();
    }
}