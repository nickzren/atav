package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class HGMDOutput extends Output {

    private String variantClass;
    private String pmid;
    private String diseaseName;

    public static final String title
            = "Variant ID,"
            + "Variant Class,"
            + "Pmid,"
            + "Disease Name,"
            + "Flanking Count";

    public HGMDOutput(String id) {
        variantId = id;

        table = "knownvar.hgmd";

        initHGMD();

        initFlankingCount();
    }

    private void initHGMD() {
        try {
            String[] tmp = variantId.split("-"); // chr-pos-ref-alt

            String sql = "SELECT variantClass,"
                    + "pmid,"
                    + "DiseaseName "
                    + "From " + table + " "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos=" + tmp[1] + " "
                    + "AND ref='" + tmp[2] + "' "
                    + "AND alt='" + tmp[3] + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                variantClass = FormatManager.getString(rs.getString("variantClass"));
                pmid = FormatManager.getString(rs.getString("pmid"));
                diseaseName = FormatManager.getString(rs.getString("DiseaseName"));
            }

            while (rs.next()) // for variant that having multi annotations
            {
                variantClass += " | " + FormatManager.getString(rs.getString("variantClass"));
                pmid += " | " + FormatManager.getString(rs.getString("pmid"));
                diseaseName += " | " + FormatManager.getString(rs.getString("DiseaseName"));
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
        sb.append(FormatManager.getString(variantClass)).append(",");
        sb.append(FormatManager.getString(pmid)).append(",");
        sb.append(FormatManager.getString(diseaseName)).append(",");

        sb.append(flankingCount);

        return sb.toString();
    }
}
