package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class Clinvar {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private String clinicalSignificance;
    private String otherIds;
    private String diseaseName;
    int flankingCount;

    public Clinvar(String id) {
        initBasic(id);

        initClinvar();
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

    private void initClinvar() {
        try {
            String sql = KnownVarManager.getSql4Clinvar(chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                clinicalSignificance = rs.getString("ClinicalSignificance".replaceAll(";", " | "));
                otherIds = rs.getString("OtherIds").replaceAll(",", " | ");
                diseaseName = rs.getString("DiseaseName").replaceAll(",", "");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        flankingCount = KnownVarManager.getFlankingCount(isSnv, chr, pos,
                KnownVarManager.clinvarTable);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getString(clinicalSignificance)).append(",");
        sb.append(FormatManager.getString(otherIds)).append(",");
        sb.append(FormatManager.getString(diseaseName)).append(",");
        sb.append(flankingCount);

        return sb.toString();
    }
}