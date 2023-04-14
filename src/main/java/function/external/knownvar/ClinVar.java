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

    private String ClinRevStar;
    private String ClinSig;
    private String ClinSigConf;
    private String DiseaseName;
    
    private boolean isPLP;

    public ClinVar(String chr, int pos, String ref, String alt,
            String ClinRevStar,
            String ClinSig,
            String ClinSigConf,
            String DiseaseName) {

        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        this.ClinRevStar = ClinRevStar;
        this.ClinSig = ClinSig;
        this.ClinSigConf = ClinSigConf;
        this.DiseaseName = DiseaseName;
        
        this.isPLP = ClinSig.startsWith("Pathogenic")
                || ClinSig.startsWith("Likely_pathogenic")
                || (ClinSig.startsWith("Conflicting_interpretations_of_pathogenicity")
                && (ClinSigConf.startsWith("Pathogenic") || ClinSigConf.startsWith("Likely_pathogenic"))
                && !ClinSigConf.contains("Benign") && !ClinSigConf.contains("Likely_benign"));
    }
    
    public String getSiteId() {
        return chr + "-" + pos;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public String getClinRevStar() {
        return ClinRevStar;
    }

    public void setClinRevStar(String ClinRevStar) {
        this.ClinRevStar = ClinRevStar;
    }

    public String getClinSig() {
        return ClinSig;
    }

    public void setClinSig(String ClinSig) {
        this.ClinSig = ClinSig;
    }

    public String getClinSigConf() {
        return ClinSigConf;
    }

    public void setClinSigConf(String ClinSigConf) {
        this.ClinSigConf = ClinSigConf;
    }

    public String getDiseaseName() {
        return DiseaseName;
    }

    public void setDiseaseName(String DiseaseName) {
        this.DiseaseName = DiseaseName;
    }

    public void append(
            String ClinRevStar,
            String ClinSig,
            String ClinSigConf,
            String DiseaseName) {
        this.ClinRevStar += " | " + ClinRevStar;
        this.ClinSig += " | " + ClinSig;
        this.ClinSigConf += " | " + ClinSigConf;
        this.DiseaseName += " | " + DiseaseName;
    }

    public boolean isPLP() {
        return this.isPLP;
    }
}
