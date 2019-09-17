package function.external.mtr;

import function.external.base.DataManager;
import function.variant.base.Region;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class MTRManager {

    static final String table = "mtr.variant_v2";

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("MTR");
        sj.add("MTR FDR");
        sj.add("MTR Centile");

        return sj.toString();
    }

    public static String getVersion() {
        return "MTR: " + DataManager.getVersion(table) + "\n";
    }

    public static String getSql4MTR(Region region) {
        return "SELECT * "
                + "FROM " + MTRManager.table + " "
                + "WHERE chr = '" + region.getChrStr() + "' AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();
    }

    public static String getSql4MTR(String chr, int pos) {
        return "SELECT MTR,FDR,MTR_centile "
                + "FROM " + MTRManager.table + " "
                + "WHERE chr = '" + chr + "' AND pos = " + pos;
    }
}
