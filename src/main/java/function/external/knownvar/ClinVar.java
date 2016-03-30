package function.external.knownvar;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinVar {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private String clinicalSignificance;
    private String otherIds;
    private String diseaseName;
    private String pubmedID;
    int flankingCount;

    public ClinVar(String chr, int pos, String ref, String alt, String clinicalSignificance,
            String otherIds, String diseaseName, String pubmedID) {
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
        this.pubmedID = pubmedID;
    }

    public void initFlankingCount() {
        flankingCount = KnownVarManager.getFlankingCount(isSnv, chr, pos,
                KnownVarManager.clinVarTable);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }
    
    public String getChr(){
        return chr;
    }
    
    public int getPos(){
        return pos;
    }
    
    public String getRef(){
        return ref;
    }
    
    public String getAlt(){
        return alt;
    }
    
    public boolean isSnv(){
        return isSnv;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getString(clinicalSignificance)).append(",");
        sb.append(FormatManager.getString(otherIds)).append(",");
        sb.append(FormatManager.getString(diseaseName)).append(",");
        sb.append(FormatManager.getString(pubmedID)).append(",");
        sb.append(flankingCount).append(",");

        return sb.toString();
    }
}
