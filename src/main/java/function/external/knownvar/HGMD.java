package function.external.knownvar;

/**
 *
 * @author nick
 */
public class HGMD {

    private String chr;
    private int pos;
    private String ref;
    private String alt;

    // hgmd
    private String variantClass;
    private String pmid;
    private String diseaseName;
    private byte isInClinVar;

    public HGMD(String chr, int pos, String ref, String alt, String variantClass,
            String pmid, String diseaseName, byte isInClinVar) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        this.variantClass = variantClass;
        this.pmid = pmid;
        this.diseaseName = diseaseName;
        this.isInClinVar = isInClinVar;
    }

    public String getSiteId() {
        return chr + "-" + pos;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public String getChr() {
        return chr;
    }

    public int getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getVariantClass() {
        return variantClass;
    }

    public void setVariantClass(String variantClass) {
        this.variantClass = variantClass;
    }
    
    public boolean isHGMDDM() {
        return variantClass.equals("DM") && isInClinVar != 1;
    }
    
    public void append(String diseaseName, String pmid, String variantClass) {
        this.diseaseName += " | " + diseaseName;
        this.pmid += " | " + pmid;
        this.variantClass += " | " + variantClass;
    }
}
