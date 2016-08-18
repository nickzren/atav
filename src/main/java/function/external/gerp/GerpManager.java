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

    public static String getTitle() {
        String title = "";

        if (GerpCommand.isIncludeGerp) {
            title = "Gerp RS Score,";
        }

        return title;
    }

    public static String getVersion() {
        if (GerpCommand.isIncludeGerp) {
            return "Gerp: " + DataManager.getVersion(table) + "\n";
        } else {
            return "";
        }
    }
    
    public static float getScore(String chr, int pos, String ref, String alt) {
        if (ref.length() > 1
                || alt.length() > 1) { // indels
            return Data.NA;
        }

        if (chr.equalsIgnoreCase("MT")) { // not support MT regions
            return Data.NA;
        }

        if (!gerp.isSameSite(chr, pos)) {
            try {
                String sql = "SELECT gerp_rs "
                        + "FROM " + table + chr + " "
                        + "WHERE pos = " + pos;

                ResultSet rs = DBManager.executeQuery(sql);

                float score = Data.NA;

                if (rs.next()) {
                    score = rs.getFloat("gerp_rs");

                }

                gerp.setValues(chr, pos, score);
            } catch (SQLException ex) {
                ErrorManager.send(ex);
            }
        }

        return gerp.getScore();
    }
}
