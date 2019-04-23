package function.external.revel;

import function.external.base.DataManager;
import function.variant.base.Region;
import global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class RevelManager {

    static final String variantTable = "revel.variant_060316";
    static final String mnvTable = "revel.mnv_060316";
    
    public static String getTitle() {
        return "REVEL";
    }

    public static String getVersion() {
        return "REVEL: " + DataManager.getVersion(variantTable) + "\n";
    }

    public static String getSqlByRegion(Region region) {
        return "SELECT chr,pos,ref,alt,aaref,aaalt,REVEL as revel "
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();
    }

    public static float getRevel(String chr, int pos, String ref, String alt, boolean isMNV) throws SQLException {
        String sql = getSqlByVariant(chr, pos, ref, alt, isMNV);

        ResultSet rs = DBManager.executeQuery(sql);

        if (rs.next()) {
            return rs.getFloat("revel");
        }

        return Data.FLOAT_NA;
    }

    public static String getSqlByVariant(String chr,
            int pos, String ref, String alt, boolean isMNV) {
        String table = variantTable;
        if(isMNV) {
            table = mnvTable;
        }
        
        return "SELECT MAX(REVEL) as revel "
                + "FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref = '" + ref + "' "
                + "AND alt = '" + alt + "' "
                + "GROUP BY chr,pos,ref,alt";
    }
}
