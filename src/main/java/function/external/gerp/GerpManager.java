package function.external.gerp;

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
public class GerpManager {

    static final String table = "gerp_2011_12_27.gerp_chr";
    private static Gerp gerp = new Gerp();

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
                String sql = "SELECT gerp_rs "
                        + "FROM " + table + chr + " "
                        + "WHERE pos = " + pos;

                ResultSet rs = DBManager.executeQuery(sql);

                float score = Data.FLOAT_NA;

                if (rs.next()) {
                    score = rs.getFloat("gerp_rs");

                }
                
                rs.close();

                gerp.setValues(chr, pos, score);
            } catch (SQLException ex) {
                ErrorManager.send(ex);
            }
        }

        return gerp.getScore();
    }
}
