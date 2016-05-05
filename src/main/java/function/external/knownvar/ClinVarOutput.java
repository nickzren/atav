package function.external.knownvar;

import java.util.Collection;

/**
 *
 * @author nick
 */
public class ClinVarOutput {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private ClinVar clinvar;

    private int flankingCount;
    private int siteCount;

    public ClinVarOutput(String chr, int pos, String ref, String alt,
            Collection<ClinVar> collection) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = true;
        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }

        clinvar = getClinVar(collection);

        flankingCount = KnownVarManager.getFlankingCount(isSnv, chr, pos,
                KnownVarManager.clinVarTable);

        siteCount = collection.size();
    }

    /*
     1. get ClinVar by matching chr-pos-ref-alt
     2. or return a site count
     */
    private ClinVar getClinVar(Collection<ClinVar> collection) {
        for (ClinVar var : collection) {
            if (getVariantId().equals(var.getVariantId())) {
                return var;
            }
        }

        return new ClinVar(chr, pos, ref, alt, "NA", "NA", "NA", "NA");
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(clinvar.getClinicalSignificance()).append(",");
        sb.append(clinvar.getOtherIds()).append(",");
        sb.append(clinvar.getDiseaseName()).append(",");
        sb.append(clinvar.getPubmedID()).append(",");
        sb.append(flankingCount).append(",");
//        sb.append(siteCount).append(",");

        return sb.toString();
    }
}
