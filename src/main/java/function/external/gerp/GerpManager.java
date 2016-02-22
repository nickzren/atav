package function.external.gerp;

import global.Data;
import java.sql.ResultSet;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class GerpManager {

    static final String table = "gerp_2011_12_27.gerp_chr";
    private static Gerp gerp = new Gerp();

    public static String getTitle() {
        String title = "Gerp RS Score,";

        return title;
    }

    public static float getScore(String id) throws Exception {
        String[] tmp = id.split("-");
        String chr = tmp[0];

        if (chr.equalsIgnoreCase("XY")) {
            chr = "X";
        }

        int pos = Integer.valueOf(tmp[1]);

        if (tmp[2].length() > 1
                || tmp[3].length() > 1) { // indels
            return Data.NA;
        } 
        
        if (chr.equalsIgnoreCase("MT")) { // not support MT regions
            return Data.NA;
        }

        if (!gerp.isSameSite(chr, pos)) {
            String sql = "SELECT gerp_rs "
                    + "FROM " + table + chr + " "
                    + "WHERE pos = " + pos;

            ResultSet rs = DBManager.executeQuery(sql);

            float score = Data.NA;

            if (rs.next()) {
                score = rs.getFloat("gerp_rs");

            }

            gerp.setValues(chr, pos, score);
        }

        return gerp.getScore();
    }
}
