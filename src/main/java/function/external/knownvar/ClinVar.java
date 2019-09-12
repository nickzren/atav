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

    private String HGVS;
    private String ClinSource;
    private String AlleleOrigin;
    private String ClinRevStat;
    private String ClinRevStar;
    private String ClinSig;
    private String ClinSigIncl;
    private String DiseaseDB;
    private String DiseaseName;
    private String PubmedID;
    private String rsID;

    public ClinVar(String chr, int pos, String ref, String alt,
            String HGVS, 
            String ClinSource,
            String AlleleOrigin, 
            String ClinRevStat,
            String ClinRevStar,
            String ClinSig,
            String ClinSigIncl,
            String DiseaseDB,
            String DiseaseName,
            String PubmedID,
            String rsID) {
        
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        this.HGVS = HGVS;
        this.ClinSource = ClinSource;
        this.AlleleOrigin = AlleleOrigin;
        this.ClinRevStat = ClinRevStat;
        this.ClinRevStar = ClinRevStar;
        this.ClinSig = ClinSig;
        this.ClinSigIncl = ClinSigIncl;
        this.DiseaseDB = DiseaseDB;
        this.DiseaseName = DiseaseName;
        this.PubmedID = PubmedID;
        this.rsID = rsID;
    }

    public String getSiteId() {
        return chr + "-" + pos;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }
    
    public String getHGVS() {
        return HGVS;
    }

    public void setHGVS(String HGVS) {
        this.HGVS = HGVS;
    }

    public String getClinSource() {
        return ClinSource;
    }

    public void setClinSource(String ClinSource) {
        this.ClinSource = ClinSource;
    }
    
    public String getAlleleOrigin() {
        return AlleleOrigin;
    }

    public void setAlleleOrigin(String AlleleOrigin) {
        this.AlleleOrigin = AlleleOrigin;
    }
    
    public String getClinRevStat() {
        return ClinRevStat;
    }

    public void setClinRevStat(String ClinRevStat) {
        this.ClinRevStat = ClinRevStat;
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
    
    public String getClinSigIncl() {
        return ClinSigIncl;
    }

    public void setClinSigIncl(String ClinSigIncl) {
        this.ClinSigIncl = ClinSigIncl;
    }
    
    public String getDiseaseDB() {
        return DiseaseDB;
    }

    public void setDiseaseDB(String DiseaseDB) {
        this.DiseaseDB = DiseaseDB;
    }
    
    public String getDiseaseName() {
        return DiseaseName;
    }

    public void setDiseaseName(String DiseaseName) {
        this.DiseaseName = DiseaseName;
    }

    public String getPubmedID() {
        return PubmedID;
    }

    public void setPubmedID(String PubmedID) {
        this.PubmedID = PubmedID;
    }
    
    public String getRSID() {
        return rsID;
    }

    public void setRSID(String rsID) {
        this.rsID = rsID;
    }

    public void append(
            String HGVS, 
            String ClinSource,
            String AlleleOrigin, 
            String ClinRevStat,
            String ClinRevStar,
            String ClinSig,
            String ClinSigIncl,
            String DiseaseDB,
            String DiseaseName,
            String PubmedID,
            String rsID) {
        this.HGVS += " | " + HGVS;
        this.ClinSource += " | " + ClinSource;
        this.AlleleOrigin += " | " + AlleleOrigin;
        this.ClinRevStat += " | " + ClinRevStat;
        this.ClinRevStar += " | " + ClinRevStar;
        this.ClinSig += " | " + ClinSig;
        this.ClinSigIncl += " | " + ClinSigIncl;
        this.DiseaseDB += " | " + DiseaseDB;
        this.DiseaseName += " | " + DiseaseName;
        this.PubmedID += " | " + PubmedID;
        this.rsID += " | " + rsID;
    }
}