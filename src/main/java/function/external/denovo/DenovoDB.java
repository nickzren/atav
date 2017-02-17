package function.external.denovo;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class DenovoDB {

    private String chr;
    private int pos;
    private String ref;
    private String alt;

    private String phenotyp;
    private String pubmedID;

    public DenovoDB(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        try {
            String sql = DenovoDBManager.getSql(chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                phenotyp = rs.getString("Phenotype");
                pubmedID = rs.getString("PubmedID");
            } else {
                phenotyp = "NA";
                pubmedID = "NA";
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public DenovoDB(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref");
            alt = rs.getString("alt");

            phenotyp = rs.getString("Phenotype");
            pubmedID = rs.getString("PubmedID");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getString(phenotyp)).append(",");
        sb.append(FormatManager.getString(pubmedID)).append(",");

        return sb.toString();
    }
}
