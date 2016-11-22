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

    private String rsNumber;
    private String clinicalSignificance;
    private String diseaseName;
    private String pubmedID;

    public ClinVar(String chr, int pos, String ref, String alt,
            String rsNumber, String clinicalSignificance,
            String diseaseName, String pubmedID) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        this.rsNumber = rsNumber;
        this.clinicalSignificance = clinicalSignificance;
        this.diseaseName = diseaseName;
        this.pubmedID = pubmedID;
    }

    public String getSiteId() {
        return chr + "-" + pos;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public String getRsNumber() {
        return rsNumber;
    }

    public void setRsNumber(String rsNumber) {
        this.rsNumber = rsNumber;
    }

    public String getClinicalSignificance() {
        return clinicalSignificance;
    }

    public void setClinicalSignificance(String clinicalSignificance) {
        this.clinicalSignificance = clinicalSignificance;
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

    public void append(String rsNumber, String clinicalSignificance, String diseaseName, String pubmedID) {
        this.rsNumber += " | " + rsNumber;
        this.clinicalSignificance += " | " + clinicalSignificance;
        this.diseaseName += " | " + diseaseName;
        this.pubmedID += " | " + pubmedID;
    }
}
