package function.external.knownvar;

/**
 *
 * @author nick
 */
public class ClinVar {

    private String chr;
    private int pos;
    private String ref;
    private String alt;

    private String clinicalSignificance;
    private String otherIds;
    private String diseaseName;
    private String pubmedID;

    public ClinVar(String chr, int pos, String ref, String alt, String clinicalSignificance,
            String otherIds, String diseaseName, String pubmedID) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        this.clinicalSignificance = clinicalSignificance;
        this.otherIds = otherIds;
        this.diseaseName = diseaseName;
        this.pubmedID = pubmedID;
    }

    public String getSiteId() {
        return chr + "-" + pos;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public String getClinicalSignificance() {
        return clinicalSignificance;
    }

    public void setClinicalSignificance(String clinicalSignificance) {
        this.clinicalSignificance = clinicalSignificance;
    }

    public String getOtherIds() {
        return otherIds;
    }

    public void setOtherIds(String otherIds) {
        this.otherIds = otherIds;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getPubmedID() {
        return pubmedID;
    }

    public void setPubmedID(String pubmedID) {
        this.pubmedID = pubmedID;
    }

    public void append(String clinicalSignificance, String otherIds, String diseaseName, String pubmedID) {
        this.clinicalSignificance += " | " + clinicalSignificance;
        this.otherIds += " | " + otherIds;
        this.diseaseName += " | " + diseaseName;
        this.pubmedID += " | " + pubmedID;
    }
}
