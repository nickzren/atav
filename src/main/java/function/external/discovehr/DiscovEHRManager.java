package function.external.discovehr;

import function.external.base.DataManager;
import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class DiscovEHRManager {

    static final String table = "knownvar.discovEHR_2017_07_31";

    public static String getTitle() {
        return "DiscovEHR AF";
    }

    public static String getVersion() {
        return "DiscovEHR: " + DataManager.getVersion(table) + "\n";
    }

    public static String getSql4AF(Region region) {
        return "SELECT * "
                + "FROM " + DiscovEHRManager.table + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();
    }

    public static String getSql4AF(String chr,
            int pos, String ref, String alt) {
        return "SELECT af "
                + "FROM " + DiscovEHRManager.table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";
    }
}
