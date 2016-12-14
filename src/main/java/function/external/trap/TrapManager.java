package function.external.trap;

import function.external.base.DataManager;
import global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class TrapManager {

    static final String table = "trap.snv_chr";

    public static String getTitle() {
        String title = "";

        if (TrapCommand.isIncludeTrap) {
            title = "TraP Score,";
        }

        return title;
    }

    public static String getVersion() {
        if (TrapCommand.isIncludeTrap) {
            return "TraP: " + DataManager.getVersion(table) + "\n";
        } else {
            return "";
        }
    }

    public static float getScore(String chr, int pos, String alt, String gene) {
        try {
            String sql = "SELECT hgnc_gene,score "
                    + "FROM " + table + chr + " "
                    + "WHERE pos = " + pos + " "
                    + "AND alt ='" + alt + "' "
                    + "AND hgnc_gene = '" + gene + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                return rs.getFloat("score");
            }
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return Data.FLOAT_NA;
    }

    public static ArrayList<Trap> getTrapList(String chr, int pos, String alt) {
        ArrayList<Trap> list = new ArrayList<>();

        try {
            String sql = "SELECT hgnc_gene,score "
                    + "FROM " + table + chr + " "
                    + "WHERE pos = " + pos + " "
                    + "AND alt ='" + alt + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String gene = rs.getString("hgnc_gene");
                float score = rs.getFloat("score");

                list.add(new Trap(gene, score));
            }
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return list;
    }
}
