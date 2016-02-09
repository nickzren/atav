package function.external.knownvar;

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

    public Clinvar(String chr, int pos, String ref, String alt, String clinicalSignificance,
            String otherIds, String diseaseName) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = true;
        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }

        this.clinicalSignificance = clinicalSignificance;
        this.otherIds = otherIds;
        this.diseaseName = diseaseName;
    }

    public void initFlankingCount() {
        flankingCount = KnownVarManager.getFlankingCount(isSnv, chr, pos,
                KnownVarManager.clinvarTable);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
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
