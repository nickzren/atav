package function.external.mpc;

import function.external.base.DataManager;
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
public class MPCManager {
    static final String table = "mpc.variant";

    private static PreparedStatement preparedStatement4Variant;
    
    public static void init() {
        if(MPCCommand.isInclude) {
            String sql = "SELECT MPC FROM " + table + " WHERE chr=? AND pos=? AND alt=?";
            preparedStatement4Variant = DBManager.initPreparedStatement(sql);
        }
    }
    
    public static String getHeader() {
        return "MPC";
    }
    
    public static String getVersion() {
        return "MPC: " + DataManager.getVersion(table) + "\n";
    }
    
    public static float getScore(String chr, int pos, String ref, String alt) {
        if (ref.length() > 1
                || alt.length() > 1) { // indels
            return Data.FLOAT_NA;
        }

        if (chr.equalsIgnoreCase("MT")) { // not support MT regions
            return Data.FLOAT_NA;
        }

        float mpc = Data.FLOAT_NA;
        
        try {
            preparedStatement4Variant.setString(1, chr);
            preparedStatement4Variant.setInt(2, pos);
            preparedStatement4Variant.setString(3, alt);
            ResultSet rs = preparedStatement4Variant.executeQuery();
            if (rs.next()) {
                mpc = rs.getFloat("MPC");

            }
            
            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return mpc;
    }
}
