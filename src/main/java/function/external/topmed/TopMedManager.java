package function.external.topmed;

import function.external.base.DataManager;
import global.Data;
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
public class TopMedManager {

    private static final String table = "topmed.variant_chr";

    public static String getHeader() {
        return "TopMed AF";
    }

    public static String getVersion() {
        return "TopMed: " + DataManager.getVersion(table) + "\n";
    }

    public static float getAF(String variantID) {
        String[] tmp = variantID.split("-");
        String chr = tmp[0];
        String pos = tmp[1];
        String ref = tmp[2];
        String alt = tmp[3];

        float af = Data.FLOAT_NA;

        if (chr.equalsIgnoreCase("Y") || chr.equalsIgnoreCase("MT")) { // not support Y and MT regions
            return af;
        }

        try {
            String sql = "SELECT af FROM " + table + chr
                    + " WHERE pos = " + pos + " AND ref = '" + ref + "' AND alt = '" + alt + "'";

            ResultSet rs = DBManager.executeQuery(sql);

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
