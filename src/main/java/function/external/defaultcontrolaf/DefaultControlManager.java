package function.external.defaultcontrolaf;

import function.external.base.DataManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class DefaultControlManager {

    private static final String table = "igm_af.default_control_variant_030421";
    private static PreparedStatement preparedStatement;

    public static void init() {
        if (DefaultControlCommand.getInstance().isInclude) {
            String sql = "SELECT ac,af,nhom FROM " + table + " WHERE chr=? AND pos=? AND ref=? AND alt=?";
            preparedStatement = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Default Control AC");
        sj.add("Default Control AF");
        sj.add("Default Control NHOM");

        return sj.toString();
    }

    public static String getVersion() {
        return "Default Control AF: " + DataManager.getVersion(table) + "\n";
    }

    public static DefaultControl getDefaultControlAF(String chr, int pos, String ref, String alt) {
        DefaultControl defaultControlAF = new DefaultControl();
        
        try {
            preparedStatement.setString(1, chr);
            preparedStatement.setInt(2, pos);
            preparedStatement.setString(3, ref);
            preparedStatement.setString(4, alt);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int ac = rs.getInt("ac");
                float af = rs.getFloat("af");
                int nhom = rs.getInt("nhom");
                
                defaultControlAF.init(ac, af, nhom);
            }

            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return defaultControlAF;
    }
}
