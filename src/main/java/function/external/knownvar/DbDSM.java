package function.external.knownvar;

/**
 *
 * @author nick
 */
public class DbDSM {

    private String chr;
    private int pos;
    private String ref;
    private String alt;

    private String diseaseName;
    private String classification;
    private String pubmedID;

    public DbDSM(String chr, int pos, String ref, String alt,
            String diseaseName, String classification, String pubmedID) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        this.diseaseName = diseaseName;
        this.classification = classification;
        this.pubmedID = pubmedID;
    }

    public String getSiteId() {
        return chr + "-" + pos;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getPubmedID() {
        return pubmedID;
    }

    public void setPubmedID(String pubmedID) {
        this.pubmedID = pubmedID;
    }

    public void append(String diseaseName, String classification, String pubmedID) {
        this.diseaseName += " | " + diseaseName;
        this.classification = " | " + classification;
        this.pubmedID += " | " + pubmedID;
    }
}
