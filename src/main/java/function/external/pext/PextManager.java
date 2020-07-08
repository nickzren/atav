package function.external.pext;

import function.external.base.DataManager;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class PextManager {

    static final String table = "pext.pext";

    private static String chr = Data.STRING_NA;
    private static int pos = Data.INTEGER_NA;
    private static float ratio = Data.FLOAT_NA;

    private static PreparedStatement preparedStatement4Site;

    public static void init() {
        if (PextCommand.isInclude) {
            String sql = "SELECT ratio FROM " + table + " WHERE chr=? AND pos=?";
            preparedStatement4Site = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
        return "PEXT Ratio";
    }

    public static String getVersion() {
        return "PEXT: " + DataManager.getVersion(table) + "\n";
    }

    public static float getRatio(String _chr, int _pos) {
        if (chr.equals(_chr) && pos == _pos) {
            return ratio;
        } else {
            chr = _chr;
            pos = _pos;
        }

        try {
            preparedStatement4Site.setString(1, chr);
            preparedStatement4Site.setInt(2, pos);
            ResultSet rs = preparedStatement4Site.executeQuery();
            if (rs.next()) {
                ratio = rs.getFloat("ratio");
            } else {
                ratio = Data.FLOAT_NA;
            }

            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return ratio;
    }
}
