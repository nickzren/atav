package function.external.acmg;

import function.external.base.DataManager;
import java.sql.ResultSet;
import java.util.HashMap;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ACMGManager {

    public static final String acmgTable = "knownvar.acmg_v3";
    private static final HashMap<String, String> acmgMap = new HashMap<>();

    public static String getHeader() {
        return "ACMG";
    }

    public static String getVersion() {
        return "ACMG: " + DataManager.getVersion(acmgTable) + "\n";
    }

    public static void init() {
        if (ACMGCommand.isInclude) {
            initMap();
        }
    }

    private static void initMap() {
        try {
            String sql = "SELECT * From " + acmgTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String gene = rs.getString("gene").toUpperCase();
                String acmg = rs.getString("ACMG");
                acmgMap.put(gene, acmg);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getACMG(String geneName) {
        return FormatManager.getString(acmgMap.get(geneName));
    }
}
