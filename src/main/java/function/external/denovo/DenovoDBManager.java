package function.external.denovo;

import function.external.base.DataManager;
import java.sql.PreparedStatement;
import java.util.StringJoiner;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class DenovoDBManager {

    static final String table = "knownvar.denovodb_2018_05_07";

    private static PreparedStatement preparedStatement4Variant;
    private static PreparedStatement preparedStatement4Region;

    public static void init() {
        if (DenovoDBCommand.isInclude) {
            String sql = "SELECT Phenotype, PubmedID FROM " + table + " WHERE chr=? AND pos=? AND ref=? AND alt=?";
            preparedStatement4Variant = DBManager.initPreparedStatement(sql);

            sql = "SELECT * FROM " + table + " WHERE chr=? AND pos BETWEEN ? AND ?";
            preparedStatement4Region = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("DenovoDB Phenotype");
//        sj.add("DenovoDB PubmedID");

        return sj.toString();
    }

    public static String getVersion() {
        return "DenovoDB: " + DataManager.getVersion(table) + "\n";
    }

    public static PreparedStatement getPreparedStatement4Variant() {
        return preparedStatement4Variant;
    }

    public static PreparedStatement getPreparedStatement4Region() {
        return preparedStatement4Region;
    }
}
