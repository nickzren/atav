package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class OMIMOutput {

    public static final String table = "knownvar.omim_2015_07_15";
    
    AnnotatedVariant annotatedVar;
    String diseaseName;

    public static final String title
            = "Variant ID,"
            + "Gene Name,"
            + "Disease Name";

    public OMIMOutput(AnnotatedVariant annotatedVar) {
        this.annotatedVar = annotatedVar;

        initOMIM();
    }

    private void initOMIM() {
        try {
            String sql = "SELECT diseaseName "
                    + "From " + table + " "
                    + "WHERE geneName='" + annotatedVar.getGeneName() + "' ";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                diseaseName = FormatManager.getString(rs.getString("diseaseName"));
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(annotatedVar.variantIdStr).append(",");
        sb.append("'").append(annotatedVar.getGeneName()).append("'").append(",");
        sb.append(FormatManager.getString(diseaseName));

        return sb.toString();
    }
}
