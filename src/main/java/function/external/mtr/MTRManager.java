package function.external.mtr;

import function.external.base.DataManager;
import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class MTRManager {

    static final String table = "mtr.variant_chr";

    public static String getTitle() {
        if (MTRCommand.isIncludeMTR) {
            return "MTR,"
                    + "MTR FDR,"
                    + "MTR Centile,";
        } else {
            return "";
        }
    }

    public static String getVersion() {
        if (MTRCommand.isIncludeMTR) {
            return "MTR: " + DataManager.getVersion(table) + "\n";
        } else {
            return "";
        }
    }

    public static String getSql4MTR(Region region) {
        return "SELECT * "
                + "FROM " + MTRManager.table + region.getChrStr() + " "
                + "WHERE Genomic_position BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();
    }

    public static String getSql4MTR(String chr, int pos) {
        return "SELECT Feature,MTR,FDR,MTR_centile "
                + "FROM " + MTRManager.table + chr + " "
                + "WHERE Genomic_position = " + pos;
    }
}
