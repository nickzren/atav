package function.test;

import function.variant.base.RegionManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class CountVariant {

    public static void run() throws SQLException {
        long uniqueVarCount = 0;
        long calledVarCount = 0;
//            ConvertCalledVariant.run();
        for (String chr : RegionManager.ALL_CHR) {
            String sql = "select count(distinct variant_id) count from WalDB.variant_chr" + chr;
            ResultSet rset = DBManager.executeQuery(sql);

            if (rset.next()) {
                uniqueVarCount += rset.getLong("count");
            }
            
            sql = "select count(*) count from WalDB.called_variant_chr" + chr;
            rset = DBManager.executeQuery(sql);

            if (rset.next()) {
                calledVarCount += rset.getLong("count");
            }
        }

        LogManager.writeAndPrint("total unique variant: " + uniqueVarCount);
        LogManager.writeAndPrint("total called variant: " + calledVarCount);
    }
}
