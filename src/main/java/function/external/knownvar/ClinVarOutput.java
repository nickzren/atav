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
    private int variant2bpflanks;
    private int variant25bpflanks;

    private boolean isClinVar = false;
    private boolean isClinVarPLP = false;
    private boolean isClinVarBLB = false;

    public ClinVarOutput(Variant var, Collection<ClinVar> collection) {
        this.var = var;

        clinvar = getClinVar(collection);

        siteCount = KnownVarManager.getClinVarPathogenicVariantFlankingCount(var, 0);

        variant2bpflanks = KnownVarManager.getClinVarPathogenicVariantFlankingCount(var, 2);
        
        variant25bpflanks = KnownVarManager.getClinVarPathogenicVariantFlankingCount(var, 25);
    }

    public static void main(String[] args) {
        System.out.println("Pathogenic,_drug_response".contains("pathogenic"));
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
                Data.STRING_NA);

        boolean isFirstSite = true;

        for (ClinVar tmpClinvar : collection) {
            String idStr = var.getVariantIdStr();

            if (idStr.equals(tmpClinvar.getVariantId())) {
                isClinVar = true;

                if (tmpClinvar.getClinSig().startsWith("Pathogenic")
                        || tmpClinvar.getClinSig().startsWith("Likely_pathogenic")
                        || (tmpClinvar.getClinSig().startsWith("Conflicting_interpretations_of_pathogenicity") 
                            && (tmpClinvar.getClinSigConf().startsWith("Pathogenic") || tmpClinvar.getClinSigConf().startsWith("Likely_pathogenic")))) {
                    isClinVarPLP = true;
                }
                
                if (tmpClinvar.getClinSig().startsWith("Benign")
                        || tmpClinvar.getClinSig().startsWith("Likely_benign")
                        || (tmpClinvar.getClinSig().startsWith("Conflicting_interpretations_of_pathogenicity") 
                            && (tmpClinvar.getClinSigConf().startsWith("Benign") || tmpClinvar.getClinSigConf().startsWith("Likely_benign"))
                            && !tmpClinvar.getClinSigConf().contains("Pathogenic") && !tmpClinvar.getClinSigConf().contains("Likely_pathogenic"))) {
                    isClinVarBLB = true;
                }

                return tmpClinvar;
            }

            if (var.isIndel()) { // site values only for SNVs
                continue;
            }

            if (isFirstSite) {
                isFirstSite = false;
                clinvar.setClinRevStar(tmpClinvar.getClinRevStar());
                clinvar.setClinSig(tmpClinvar.getClinSig());
                clinvar.setClinSigConf(tmpClinvar.getClinSigConf());
                clinvar.setDiseaseName("?Site - " + tmpClinvar.getDiseaseName());
            } else {
                clinvar.append(
                        tmpClinvar.getClinRevStar(),
                        tmpClinvar.getClinSig(),
                        tmpClinvar.getClinSigConf(),
                        tmpClinvar.getDiseaseName());
            }
        }

        return clinvar;
    }

    public boolean isClinVar() {
        return isClinVar;
    }
    
    public boolean isClinVarPLP() {
        return isClinVarPLP;
    }

    public ClinVar getClinVar() {
        return clinvar;
    }
    
    public boolean isClinVarPLPSite() {
        return siteCount > 0;
    }

    public boolean isClinVarBLB() {
        return isClinVarBLB;
    }
    
    public boolean isPLP2bpFlankingValid() {
        return variant2bpflanks > 0;
    }
    
    public boolean isPLP25bpFlankingValid() {
        return variant25bpflanks >= 6;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(siteCount));        
        sj.add(FormatManager.getInteger(variant2bpflanks));
        sj.add(FormatManager.getInteger(variant25bpflanks));
        sj.add(clinvar.getClinRevStar());
        sj.add(FormatManager.appendDoubleQuote(clinvar.getClinSig()));
        sj.add(FormatManager.appendDoubleQuote(clinvar.getClinSigConf()));

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
