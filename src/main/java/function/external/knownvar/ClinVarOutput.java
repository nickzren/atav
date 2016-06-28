package function.external.knownvar;

import function.variant.base.Variant;
import global.Data;
import java.util.Collection;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinVarOutput {

    private Variant var;

    private ClinVar clinvar;

    private int siteCount;
    private int pathogenicIndelsCount;
    private int allIndelsCount;

    public ClinVarOutput(Variant var, Collection<ClinVar> collection) {
        this.var = var;

        clinvar = getClinVar(collection);

        siteCount = var.isSnv() ? collection.size() : Data.NA; // only for SNVs

        pathogenicIndelsCount = var.isIndel() ? KnownVarManager.getClinVarPathogenicIndelFlankingCount(var) : Data.NA; // only for INDELs
        allIndelsCount = var.isIndel() ? KnownVarManager.getClinVarAllIndelFlankingCount(var) : Data.NA; // only for INDELs
    }

    /*
     1. get ClinVar by matching chr-pos-ref-alt
     2. or return site accumulated ClinVar
     */
    private ClinVar getClinVar(Collection<ClinVar> collection) {
        ClinVar clinvar = new ClinVar(
                var.getChrStr(),
                var.getStartPosition(),
                var.getRefAllele(),
                var.getAllele(),
                "NA",
                "NA",
                "NA",
                "NA");

        boolean isFirstSiteClinvar = true;

        for (ClinVar tmpClinvar : collection) {
            String idStr = var.getVariantIdStr().replaceAll("XY", "X");

            if (idStr.equals(tmpClinvar.getVariantId())) {
                return tmpClinvar;
            }

            if (var.isIndel()) { // site values only for SNVs
                continue;
            }

            if (isFirstSiteClinvar) {
                isFirstSiteClinvar = false;
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

        return clinvar;
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
