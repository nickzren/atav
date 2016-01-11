package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinGen {

    String geneName;
    String haploinsufficiencyDesc;
    String triplosensitivityDesc;

    public ClinGen(String geneName) {
        this.geneName = geneName;

        initClinGen();
    }

    private void initClinGen() {
        try {
            String sql = KnownVarManager.getSql4ClinGen(geneName);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                haploinsufficiencyDesc = rs.getString("HaploinsufficiencyDesc");
                triplosensitivityDesc = rs.getString("TriplosensitivityDesc");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getString(haploinsufficiencyDesc)).append(",");
        sb.append(FormatManager.getString(triplosensitivityDesc));

        return sb.toString();
    }
}
