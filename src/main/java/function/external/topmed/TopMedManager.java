package function.external.topmed;

import function.external.base.DataManager;
import function.variant.base.RegionManager;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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

    private static final HashMap<String, PreparedStatement> preparedStatement4VariantMap = new HashMap<>();

    public static void init() {
        if (TopMedCommand.isInclude) {
            for (String chr : RegionManager.ALL_CHR) {
                String sql = "SELECT af FROM " + table + chr + " WHERE pos=? AND ref=? AND alt=?";
                preparedStatement4VariantMap.put(chr, DBManager.initPreparedStatement(sql));
            }
        }
    }

    public static String getHeader() {
        return "TopMed AF";
    }

    public static String getVersion() {
        return "TopMed: " + DataManager.getVersion(table) + "\n";
    }

    public static float getAF(String variantID) {
        String[] tmp = variantID.split("-");
        String chr = tmp[0];
        int pos = Integer.valueOf(tmp[1]);
        String ref = tmp[2];
        String alt = tmp[3];

        float af = Data.FLOAT_NA;

        if (chr.equalsIgnoreCase("Y") || chr.equalsIgnoreCase("MT")) { // not support Y and MT regions
            return af;
        }

        try {            
            PreparedStatement preparedStatement = preparedStatement4VariantMap.get(chr);
            preparedStatement.setInt(1, pos);
            preparedStatement.setString(2, ref);
            preparedStatement.setString(3, alt);
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
