package function.external.knownvar;

import global.Data;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class KnownvarManager {

    public static int getFlankingCount(String variantId, String table) {
        try {
            String[] tmp = variantId.split("-"); // chr-pos-ref-alt

            int pos = Integer.valueOf(tmp[1]);
            int width = KnownVarCommand.snvWidth;

            if (tmp[2].length() > 1
                    || tmp[3].length() > 1) {
                width = KnownVarCommand.indelWidth;
            }

            String sql = "SELECT count(*) as count "
                    + "From " + table + " "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos BETWEEN " + (pos - width) + " AND " + (pos + width);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.NA;
    }
}
