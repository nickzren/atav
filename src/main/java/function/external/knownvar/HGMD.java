package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class HGMD {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    // hgmd
    private String variantClass;
    private String pmid;
    private String diseaseName;
    int flankingCount;

    public HGMD(String id) {
        initBasic(id);

        initHGMD();
    }

    private void initBasic(String id) {
        String[] tmp = id.split("-");
        chr = tmp[0];
        pos = Integer.valueOf(tmp[1]);
        ref = tmp[2];
        alt = tmp[3];

        isSnv = true;

        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }
    }

    private void initHGMD() {
        try {
            String sql = KnownVarManager.getSql4HGMD(chr, pos, ref, alt);

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

        flankingCount = KnownVarManager.getFlankingCount(isSnv, chr, pos,
                KnownVarManager.hgmdTable);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getString(variantClass)).append(",");
        sb.append(FormatManager.getString(pmid)).append(",");
        sb.append(FormatManager.getString(diseaseName)).append(",");
        sb.append(flankingCount);

        return sb.toString();
    }
}
