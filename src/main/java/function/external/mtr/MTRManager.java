package function.external.mtr;

import function.external.base.DataManager;
import java.sql.PreparedStatement;
import java.util.StringJoiner;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class MTRManager {

    static final String table = "mtr.variant_v2";

    private static PreparedStatement preparedStatement4Site;
    private static PreparedStatement preparedStatement4Region;

    public static void init() {
        if (MTRCommand.isInclude) {
            String sql = "SELECT MTR,FDR,MTR_centile FROM " + MTRManager.table + " WHERE chr=? AND pos=?";
            preparedStatement4Site = DBManager.initPreparedStatement(sql);

            sql = "SELECT * FROM " + MTRManager.table + " WHERE chr=? AND pos BETWEEN ? AND ?";
            preparedStatement4Region = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("MTR");
        sj.add("MTR FDR");
        sj.add("MTR Centile");

        return sj.toString();
    }

    public static String getVersion() {
        return "MTR: " + DataManager.getVersion(table) + "\n";
    }
    
    public static PreparedStatement getPreparedStatement4Site() {
        return preparedStatement4Site;
    }
    
    public static PreparedStatement getPreparedStatement4Region() {
        return preparedStatement4Region;
    }
}
