package function.external.igmaf;

import function.external.base.DataManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;
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
            String sql = "SELECT ac, af, ns, nhom FROM " + table + " WHERE chr=? AND variant_id=?";
            preparedStatement = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("IGM AC");
        sj.add("IGM AF");
        sj.add("IGM NS");
        sj.add("IGM NHOM");

        return sj.toString();
    }

    public static String getVersion() {
        return "IGM AF: " + DataManager.getVersion(table) + "\n";
    }

    public static float getAF(CSVRecord record) {
        return FormatManager.getFloat(record, "IGM AF");
    }


    public static IGMAF getIGMAF(String chr, int variantID) {
        IGMAF igmAF = new IGMAF();

        try {
            preparedStatement.setString(1, chr);
            preparedStatement.setInt(2, variantID);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int ac = rs.getInt("ac");
                float af = rs.getFloat("af");
                int ns = rs.getInt("ns");
                int nhom = rs.getInt("nhom");

                igmAF.init(ac, af, ns, nhom);
            }

            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return igmAF;
    }
}
