package function.external.knownvar;

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

    public HGMD(String chr, int pos, String ref, String alt, String variantClass,
            String pmid, String diseaseName) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = true;
        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }

        this.variantClass = variantClass;
        this.pmid = pmid;
        this.diseaseName = diseaseName;
    }

    public void initFlankingCount() {
        flankingCount = KnownVarManager.getFlankingCount(isSnv, chr, pos,
                KnownVarManager.hgmdTable);
    }

    public void append(String variantClass, String pmid, String diseaseName) {
        this.variantClass += " | " + variantClass;
        this.pmid += " | " + pmid;
        this.diseaseName += " | " + diseaseName;
    }
    
    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
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
