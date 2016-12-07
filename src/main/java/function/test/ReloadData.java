package function.test;

import function.variant.base.RegionManager;
import java.sql.SQLException;
import utils.DBManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class ReloadData {

    public static void run() throws Exception {
        for (String chr : RegionManager.ALL_CHR) {
            // variant
            String sql = "select * from variant_chr" + chr + " INTO OUTFILE '/nfs/seqscratch10/tmp/variant_chr" + chr + ".txt'";
            executeSQL(sql);

            sql = "TRUNCATE variant_chr" + chr;
            updateSQL(sql);

            sql = "load data infile '/nfs/seqscratch10/tmp/variant_chr" + chr + ".txt' into table variant_chr" + chr;
            executeSQL(sql);

            // called variant
            sql = "select * from called_variant_chr" + chr + " INTO OUTFILE '/nfs/seqscratch10/tmp/called_variant_chr" + chr + ".txt'";
            executeSQL(sql);

            sql = "TRUNCATE called_variant_chr" + chr;
            updateSQL(sql);

            sql = "load data infile '/nfs/seqscratch10/tmp/called_variant_chr" + chr + ".txt' into table called_variant_chr" + chr;
            executeSQL(sql);

            // DP bins
            sql = "select * from DP_bins_chr" + chr + " INTO OUTFILE '/nfs/seqscratch10/tmp/DP_bins_chr" + chr + ".txt'";
            executeSQL(sql);

            sql = "TRUNCATE DP_bins_chr" + chr;
            updateSQL(sql);

            sql = "load data infile '/nfs/seqscratch10/tmp/DP_bins_chr" + chr + ".txt' into table DP_bins_chr" + chr;
            executeSQL(sql);
        }
    }

    public static void executeSQL(String sql) throws SQLException {
        LogManager.writeAndPrint(sql);
        DBManager.executeQuery(sql);
    }
    
    public static void updateSQL(String sql) throws SQLException {
        LogManager.writeAndPrint(sql);
        DBManager.executeUpdate(sql);
    }
}
