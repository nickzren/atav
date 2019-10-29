package function.external.mpc;

import function.external.base.DataManager;
import global.Data;
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

    public static String getTitle() {
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
            String sql = "SELECT MPC FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND alt = '" + alt + "'";

            ResultSet rs = DBManager.executeQuery(sql);
            
            if (rs.next()) {
                mpc = rs.getFloat("MPC");

            }
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return mpc;
    }
}
