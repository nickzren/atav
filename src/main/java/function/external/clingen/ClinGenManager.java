package function.external.clingen;

import function.external.base.DataManager;
import global.Data;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinGenManager {

    public static final String clinGenTable = "knownvar.clingen_2021_04_08";
    private static final HashMap<String, ClinGen> clinGenMap = new HashMap<>();

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("ClinGen");
        sj.add("ClinGen HaploinsufficiencyDesc");
        sj.add("ClinGen TriplosensitivityDesc");

        return sj.toString();
    }

    public static String getVersion() {
        return "ClinGen: " + DataManager.getVersion(clinGenTable) + "\n";
    }

    public static void init() {
        if (ClinGenCommand.isInclude) {
            initMap();
        }
    }

    private static void initMap() {
        try {
            String sql = "SELECT * From " + clinGenTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName").toUpperCase();
                String haploinsufficiencyDesc = rs.getString("HaploinsufficiencyDesc");
                String triplosensitivityDesc = rs.getString("TriplosensitivityDesc");

                ClinGen clinGen = new ClinGen(haploinsufficiencyDesc, triplosensitivityDesc);

                clinGenMap.put(geneName, clinGen);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static ClinGen getClinGen(String geneName) {
        ClinGen clinGen = clinGenMap.get(geneName);

        if (clinGen == null) {
            clinGen = new ClinGen(Data.STRING_NA, Data.STRING_NA);
        }

        return clinGen;
    }
}
