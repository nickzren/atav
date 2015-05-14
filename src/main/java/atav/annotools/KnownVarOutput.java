package atav.annotools;

import atav.manager.utils.CommandValue;
import atav.manager.utils.DBManager;
import atav.manager.utils.ErrorManager;
import atav.manager.utils.FormatManager;
import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    private String variantId;
    private String clinicalSignificance;
    private String otherIds;
    private String diseaseName;
    private int clinvarFlankingCount;

    public static final String title
            = "Variant ID,"
            + "Clinical Significance,"
            + "Other Ids,"
            + "Disease Name,"
            + "Clinvar Flanking Count";

    public KnownVarOutput(String id) {
        variantId = id;

        initClinvar();

        initFlankingCount();
    }

    private void initClinvar() {
        try {
            String[] tmp = variantId.split("-"); // chr-pos-ref-alt

            String sql = "SELECT ClinicalSignificance,"
                    + "OtherIds,"
                    + "DiseaseName "
                    + "From knownvar.clinvar_2015_04_10 "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos=" + tmp[1] + " "
                    + "AND ref='" + tmp[2] + "' "
                    + "AND alt='" + tmp[3] + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                clinicalSignificance = FormatManager.getString(rs.getString("ClinicalSignificance"));
                otherIds = FormatManager.getString(rs.getString("OtherIds")).replaceAll(",", ";");
                diseaseName = FormatManager.getString(rs.getString("DiseaseName"));
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initFlankingCount() {
        try {
            String[] tmp = variantId.split("-"); // chr-pos-ref-alt

            int pos = Integer.valueOf(tmp[1]);
            int width = CommandValue.snvWidth;

            if (tmp[2].length() > 1
                    || tmp[3].length() > 1) {
                width = CommandValue.indelWidth;
            }

            String sql = "SELECT count(*) as count "
                    + "From knownvar.clinvar "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos BETWEEN " + (pos - width) + " AND " + (pos + width);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                clinvarFlankingCount = rs.getInt("count");
            } 
            
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getString(clinicalSignificance)).append(",");
        sb.append(FormatManager.getString(otherIds)).append(",");
        sb.append(FormatManager.getString(diseaseName)).append(",");
        sb.append(clinvarFlankingCount);

        return sb.toString();
    }
}
