package function.external.revel;

import function.external.base.DataManager;
import function.variant.base.Region;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class RevelManager {

    static final String variantTable = "revel.variant_060316";
    static final String mnvTable = "revel.mnv_060316";
    
    private static PreparedStatement preparedStatement4Variant;
    private static PreparedStatement preparedStatement4MNV;
    private static PreparedStatement preparedStatement4Region;
    
    public static void init() {
        if(RevelCommand.isInclude) {
            String sql = "SELECT MAX(REVEL) as revel FROM " + variantTable + " "
                + "WHERE chr=? AND pos=? AND ref=? AND alt=? GROUP BY chr,pos,ref,alt";
            preparedStatement4Variant = DBManager.initPreparedStatement(sql);

            sql = "SELECT MAX(REVEL) as revel FROM " + mnvTable + " "
                + "WHERE chr=? AND pos=? AND ref=? AND alt=? GROUP BY chr,pos,ref,alt";
            preparedStatement4MNV = DBManager.initPreparedStatement(sql);
            
            sql =  "SELECT chr,pos,ref,alt,aaref,aaalt,REVEL as revel "
                + "FROM " + variantTable + " WHERE chr=? AND pos BETWEEN ? AND ?";
            preparedStatement4Region = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
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

    public static float getRevel(String chr, int pos, String ref, String alt, boolean isMNV) {
        try {
            PreparedStatement preparedStatement = isMNV ? preparedStatement4MNV : preparedStatement4Variant;
            preparedStatement.setString(1, chr);
            preparedStatement.setInt(2, pos);
            preparedStatement.setString(3, ref);
            preparedStatement.setString(4, alt);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getFloat("revel");
            }
            
            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return Data.FLOAT_NA;
    }
    
    public static PreparedStatement getPreparedStatement4Region() {
        return preparedStatement4Region;
    }
}
