package function.external.igmaf;

import function.external.base.DataManager;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.csv.CSVRecord;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class IGMAFManager {

    private static final String table = "igm_af.variant_101122";
    private static PreparedStatement preparedStatement;

    public static void init() {
        if (IGMAFCommand.getInstance().isInclude) {
            String sql = "SELECT af FROM " + table + " WHERE chr=? AND variant_id=?";
            preparedStatement = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
        return "IGM AF";
    }

    public static String getVersion() {
        return "IGM AF: " + DataManager.getVersion(table) + "\n";
    }

    public static float getAF(String chr, int variantID) {
        float af = Data.FLOAT_NA;

        try {
            preparedStatement.setString(1, chr);
            preparedStatement.setInt(2, variantID);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                af = rs.getFloat("af");
            }

            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return af;
    }

    public static float getAF(CSVRecord record) {
        return FormatManager.getFloat(record, getHeader());
    }
}
