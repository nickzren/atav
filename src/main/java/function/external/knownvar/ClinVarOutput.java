package function.external.knownvar;

import function.variant.base.Variant;
import global.Data;
import java.util.Collection;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinVarOutput {

    private Variant var;

    private ClinVar clinvar;

    private int siteCount;
    private int variant10bpflanks;

    private boolean isClinVarPLP = false;

    public ClinVarOutput(Variant var, Collection<ClinVar> collection) {
        this.var = var;

        clinvar = getClinVar(collection);

        siteCount = var.isSnv() ? collection.size() : Data.INTEGER_NA; // only for SNVs

        variant10bpflanks = KnownVarManager.getClinVarPathogenicIndelFlankingCount(var, var.isSnv(), 2);
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
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA);

        boolean isFirstSite = true;

        for (ClinVar tmpClinvar : collection) {
            String idStr = var.getVariantIdStr();

            if (idStr.equals(tmpClinvar.getVariantId())) {
                if (tmpClinvar.getClinSig().contains("Pathogenic")
                        && !tmpClinvar.getClinSig().contains("Conflicting_interpretations_of_pathogenicity")) {
                    isClinVarPLP = true;
                }

                return tmpClinvar;
            }

            if (var.isIndel()) { // site values only for SNVs
                continue;
            }

            if (isFirstSite) {
                isFirstSite = false;
                clinvar.setHGVS(tmpClinvar.getHGVS());
                clinvar.setClinSource(tmpClinvar.getClinSource());
                clinvar.setAlleleOrigin(tmpClinvar.getAlleleOrigin());
                clinvar.setClinRevStat(tmpClinvar.getClinRevStat());
                clinvar.setClinRevStar(tmpClinvar.getClinRevStar());
                clinvar.setClinSig(tmpClinvar.getClinSig());
                clinvar.setClinSigIncl(tmpClinvar.getClinSigIncl());
                clinvar.setDiseaseDB(tmpClinvar.getDiseaseDB());
                clinvar.setDiseaseName("?Site - " + tmpClinvar.getDiseaseName());
                clinvar.setPubmedID(tmpClinvar.getPubmedID());
                clinvar.setRSID(tmpClinvar.getRSID());
            } else {
                clinvar.append(
                        tmpClinvar.getHGVS(),
                        tmpClinvar.getClinSource(),
                        tmpClinvar.getAlleleOrigin(),
                        tmpClinvar.getClinRevStat(),
                        tmpClinvar.getClinRevStar(),
                        tmpClinvar.getClinSig(),
                        tmpClinvar.getClinSigIncl(),
                        tmpClinvar.getDiseaseDB(),
                        tmpClinvar.getDiseaseName(),
                        tmpClinvar.getPubmedID(),
                        tmpClinvar.getRSID());
            }
        }

        return clinvar;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(siteCount));
        sj.add(clinvar.getClinRevStar());
        sj.add(FormatManager.appendDoubleQuote(clinvar.getClinSig()));
        sj.add(FormatManager.appendDoubleQuote(clinvar.getDiseaseName()));
        sj.add(FormatManager.getInteger(variant10bpflanks));

        return sj;
    }

    public ClinVar getClinVar() {
        return clinvar;
    }

    public boolean isClinVarPLP() {
        return isClinVarPLP;
    }

    public boolean is10bpFlankingValid() {
        return variant10bpflanks > 0;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
