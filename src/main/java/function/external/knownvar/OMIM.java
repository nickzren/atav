package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class OMIM {
    String geneName;
    String omimDiseaseName;
    
    public OMIM(String geneName) {
        this.geneName = geneName;

        initOMIM();
    }
    
    
    private void initOMIM() {
        try {
            String sql = KnownVarManager.getSql4OMIM(geneName);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                omimDiseaseName = FormatManager.getString(rs.getString("diseaseName"));
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("'").append(geneName).append("'").append(",");
        sb.append(FormatManager.getString(omimDiseaseName));

        return sb.toString();
    }
}
