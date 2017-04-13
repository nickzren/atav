package function.test;

import function.variant.base.RegionManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class CountVariant {

    public static void run() throws SQLException {
        int count = 0;
//            ConvertCalledVariant.run();
        for (String chr : RegionManager.ALL_CHR) {
            String sql = "select count(distinct variant_id) count from WalDB.variant_chr" + chr + " "
                    + "where effect_id = 43";
            ResultSet rset = DBManager.executeQuery(sql);

            if (rset.next()) {
                count += rset.getInt("count");
            }
        }

        System.out.println("total variant: " + count);
    }
}
