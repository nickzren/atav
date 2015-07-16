package function.external.knownvar;

import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;
import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class ClinvarOutput {

    public static final String table = "knownvar.clinvar_2015_06_22";
    
    String variantId;
    private String clinicalSignificance;
    private String otherIds;
    private String diseaseName;
    int flankingCount;

    public static final String title
            = "Variant ID,"
            + "Clinical Significance,"
            + "Other Ids,"
            + "Disease Name,"
            + "Flanking Count";

    public ClinvarOutput(String id) {
        variantId = id;

        initClinvar();

        flankingCount = KnownvarManager.getFlankingCount(variantId, table);
    }

    private void initClinvar() {
        try {
            String[] tmp = variantId.split("-"); // chr-pos-ref-alt

            String sql = "SELECT ClinicalSignificance,"
                    + "OtherIds,"
                    + "DiseaseName "
                    + "From " + table + " "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos=" + tmp[1] + " "
                    + "AND ref='" + tmp[2] + "' "
                    + "AND alt='" + tmp[3] + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                clinicalSignificance = FormatManager.getString(rs.getString("ClinicalSignificance").replaceAll(";", " | "));
                otherIds = FormatManager.getString(rs.getString("OtherIds")).replaceAll(",", " | ");
                diseaseName = FormatManager.getString(rs.getString("DiseaseName")).replaceAll(",", "");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(variantId).append(",");
        sb.append(FormatManager.getString(clinicalSignificance)).append(",");
        sb.append(FormatManager.getString(otherIds)).append(",");
        sb.append(FormatManager.getString(diseaseName)).append(",");
        sb.append(flankingCount);

        return sb.toString();
    }
}
