package function.external.knownvar;

import java.sql.ResultSet;
import utils.CommandValue;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class Output {

    String variantId;
    String table;
    int flankingCount;

    void initFlankingCount() {
        try {
            String[] tmp = variantId.split("-"); // chr-pos-ref-alt

            int pos = Integer.valueOf(tmp[1]);
            int width = CommandValue.snvWidth;

            if (tmp[2].length() > 1
                    || tmp[3].length() > 1) {
                width = CommandValue.indelWidth;
            }

            String sql = "SELECT count(*) as count "
                    + "From " + table + " "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos BETWEEN " + (pos - width) + " AND " + (pos + width);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                flankingCount = rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
}
