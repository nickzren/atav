package function.external.omim;

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
public class OMIMManager {

    public static final String omimTable = "knownvar.omim_2021_07_14";
    private static final HashMap<String, String> omimMap = new HashMap<>();

    public static String getHeader() {
        return "OMIM Disease";
    }

    public static String getVersion() {
        return "OMIM: " + DataManager.getVersion(omimTable) + "\n";
    }

    public static void init() {
        if (OMIMCommand.isInclude) {
            initMap();
        }
    }

    private static void initMap() {
       if (!omimMap.isEmpty()) {
            return;
        }

        try {
            String sql = "SELECT * From " + omimTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName").toUpperCase();
                String diseaseName = rs.getString("diseaseName");
                omimMap.put(geneName, diseaseName);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getOMIM(String geneName) {
        return FormatManager.getString(omimMap.get(geneName));
    }
}
