package function.external.knownvar;

import java.util.Collection;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinVarOutput {

    private String chr;
    private int pos;
    private String ref;
    private String alt;

    private ClinVar clinvar;

    private int siteCount;
    private int pathogenicIndelsCount;
    private int allIndelsCount;

    public ClinVarOutput(String chr, int pos, String ref, String alt,
            Collection<ClinVar> collection) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        clinvar = getClinVar(collection);

        siteCount = collection.size();

        pathogenicIndelsCount = KnownVarManager.getClinVarPathogenicIndelFlankingCount(chr, pos);
        allIndelsCount = KnownVarManager.getClinVarAllIndelFlankingCount(chr, pos);
    }

    /*
     1. get ClinVar by matching chr-pos-ref-alt
     2. or return site accumulated ClinVar
     */
    private ClinVar getClinVar(Collection<ClinVar> collection) {
        ClinVar clinvar = new ClinVar(chr, pos, ref, alt, "NA", "NA", "NA", "NA");

        boolean isFirstSiteHgmd = true;

        for (ClinVar tmpClinvar : collection) {
            if (getVariantId().equals(tmpClinvar.getVariantId())) {
                return tmpClinvar;
            } else {
                if (isFirstSiteHgmd) {
                    isFirstSiteHgmd = false;
                    clinvar.setDiseaseName("?Site - " + tmpClinvar.getDiseaseName());
                    clinvar.setClinicalSignificance(tmpClinvar.getClinicalSignificance());
                    clinvar.setPubmedID(tmpClinvar.getPubmedID());
                    clinvar.setOtherIds(tmpClinvar.getOtherIds());
                } else {
                    clinvar.append(
                            tmpClinvar.getClinicalSignificance(),
                            tmpClinvar.getOtherIds(),
                            tmpClinvar.getDiseaseName(),
                            tmpClinvar.getPubmedID());
                }
            }
        }

        return clinvar;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getInteger(siteCount)).append(",");
        sb.append(clinvar.getDiseaseName()).append(",");
        sb.append(clinvar.getClinicalSignificance()).append(",");
        sb.append(clinvar.getPubmedID()).append(",");
        sb.append(clinvar.getOtherIds()).append(",");
        sb.append(FormatManager.getInteger(pathogenicIndelsCount)).append(",");
        sb.append(FormatManager.getInteger(allIndelsCount)).append(",");

        return sb.toString();
    }
}
