package function.external.gerp;

import function.external.base.DataManager;
import function.variant.base.RegionManager;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class GerpManager {

    static final String table = "gerp_2011_12_27.gerp_chr";
    private static Gerp gerp = new Gerp();

    private static final HashMap<String, PreparedStatement> preparedStatement4SiteMap = new HashMap<>();

    public static void init() {
        if (GerpCommand.isInclude) {
            for (String chr : RegionManager.ALL_CHR) {
                String sql = "SELECT gerp_rs FROM " + table + chr + " WHERE pos=?";
                preparedStatement4SiteMap.put(chr, DBManager.initPreparedStatement(sql));
            }
        }
    }

    public static String getHeader() {
        return "Gerp RS Score";
    }

    public static String getVersion() {
        return "Gerp: " + DataManager.getVersion(table) + "\n";
    }

    public static float getScore(String chr, int pos, String ref, String alt) {
        if (ref.length() > 1
                || alt.length() > 1) { // indels
            return Data.FLOAT_NA;
        }

        if (chr.equalsIgnoreCase("MT")) { // not support MT regions
            return Data.FLOAT_NA;
        }

        if (!gerp.isSameSite(chr, pos)) {
            try {
                PreparedStatement preparedStatement = preparedStatement4SiteMap.get(chr);
                preparedStatement.setInt(1, pos);
                ResultSet rs = preparedStatement.executeQuery();
                float score = rs.next() ? rs.getFloat("gerp_rs") : Data.FLOAT_NA;
                rs.close();
                gerp.setValues(chr, pos, score);
            } catch (SQLException ex) {
                ErrorManager.send(ex);
            }
        }

        return gerp.getScore();
    }
}
