package function.external.discovehr;

import function.external.base.DataManager;
import java.sql.PreparedStatement;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class DiscovEHRManager {

    static final String table = "knownvar.discovEHR_2017_07_31";
    
    private static PreparedStatement preparedStatement4Variant;
    private static PreparedStatement preparedStatement4Region;

    public static void init() {
        if (DiscovEHRCommand.isInclude) {
            String sql = "SELECT af FROM " + table + " WHERE chr=? AND pos=? AND ref_allele=? AND alt_allele=?";
            preparedStatement4Variant = DBManager.initPreparedStatement(sql);

            sql = "SELECT * FROM " + table + " WHERE chr=? AND pos BETWEEN ? AND ?";
            preparedStatement4Region = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
        return "DiscovEHR AF";
    }

    public static String getVersion() {
        return "DiscovEHR: " + DataManager.getVersion(table) + "\n";
    }
    
    public static PreparedStatement getPreparedStatement4Variant() {
        return preparedStatement4Variant;
    }

    public static PreparedStatement getPreparedStatement4Region() {
        return preparedStatement4Region;
    }
}
