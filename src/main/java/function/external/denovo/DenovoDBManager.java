package function.external.denovo;

import function.external.base.DataManager;
import function.variant.base.Region;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class DenovoDBManager {

    static final String table = "knownvar.denovodb_2018_05_07";

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("DenovoDB Phenotype");
        sj.add("DenovoDB PubmedID");

        return sj.toString();
    }

    public static String getVersion() {
        return "DenovoDB: " + DataManager.getVersion(table) + "\n";
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
