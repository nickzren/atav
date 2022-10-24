package function.external.knownvar;

import function.annotation.base.GeneManager;
import function.variant.base.Variant;
import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    private HGMDOutput hgmdOutput;
    private ClinVarOutput clinVarOutput;
    private ClinVarPathoratio clinVarPathoratio;
    private String clinGen;
    private String omimDiseaseName;
    private String omimInheritance;
    private String acmg;
    private DBDSMOutput dbDSMOutput;

    public static String getHeader() {
        return "Variant ID,"
                + "Gene Name,"
                + KnownVarManager.getHeader();
    }

    public KnownVarOutput(Variant var) {
        hgmdOutput = KnownVarManager.getHGMDOutput(var);
        clinVarOutput = KnownVarManager.getClinVarOutput(var);
    }

    public void init(String gene) {
        String geneName = GeneManager.getUpToDateGene(gene).toUpperCase();
        clinVarPathoratio = KnownVarManager.getClinPathoratio(geneName);

        clinGen = KnownVarManager.getClinGen(geneName);
        omimDiseaseName = KnownVarManager.getOMIM(geneName);
        omimInheritance = getOMIMInheritance();
//        recessiveCarrier = KnownVarManager.getRecessiveCarrier(geneName);
        acmg = KnownVarManager.getACMG(geneName);
//        dbDSMOutput = KnownVarManager.getDBDSMOutput(annotatedVar);
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(hgmdOutput.getStringJoiner());
        sj.merge(clinVarOutput.getStringJoiner());        
        sj.merge(clinVarPathoratio.getStringJoiner());
        sj.add(clinGen);
        sj.add(FormatManager.appendDoubleQuote(omimDiseaseName));
        sj.add(omimInheritance);
//        sj.add(FormatManager.getInteger(recessiveCarrier));
        sj.add(acmg);
//        sj.merge(dbDSMOutput.getStringJoiner());

        return sj;
    }

    // a variant is either HGMD "DM" (not CinVar B/LB) or ClinVar P/LP
    public boolean isKnownVariant() {
        return hgmdOutput.isHGMDDM()
                || clinVarOutput.isClinVarPLP();
    }
    
    public boolean isHGMDDM() {
        return hgmdOutput.isHGMDDM();
    }
    
    public boolean isClinVarPLP() {
        return clinVarOutput.isClinVarPLP();
    }
    
    // a site has variant is either HGMD "DM" (not CinVar B/LB) or ClinVar P/LP
    public boolean isKnownVariantSite() {
        return hgmdOutput.isHGMDDMSite()
                || clinVarOutput.isClinVarPLPSite();
    }
    
    public boolean isClinVarPLPSite() {
        return clinVarOutput.isClinVarPLPSite();
    }
    
    public boolean isHGMDDMSite() {
        return hgmdOutput.isHGMDDMSite();
    }

    // a variant is ClinVar B/LB
    public boolean isExcludeClinVarBLB() {
        if (KnownVarCommand.isExcludeClinVarBLB) {
            return !clinVarOutput.isClinVarBLB();
        }

        return true;
    }
    
    public boolean isClinVarBLB() {
        return clinVarOutput.isClinVarBLB();
    }

    public boolean isOMIMGene() {
        return !omimDiseaseName.equals(Data.STRING_NA);
    }

    public boolean isOMIMDominant() {
        return omimInheritance.contains("AD")
                || omimInheritance.contains("XLD")
                || omimInheritance.contains("PD")
                || omimInheritance.contains("DD")
                || omimInheritance.contains("SMo")
                || omimInheritance.contains("SMu")
                || omimInheritance.contains("XL");
    }

    public boolean isOMIMRecessive() {
        return omimInheritance.contains("AR")
                || omimInheritance.contains("PR")
                || omimInheritance.contains("DR")
                || omimInheritance.contains("XLR")
                || omimInheritance.contains("XL");
    }
    
    public String getOMIMInheritance() {
        StringJoiner sj = new StringJoiner("|");

        if (omimDiseaseName.contains("Autosomal dominant")) {
            sj.add("AD");
        }

        if (omimDiseaseName.contains("Autosomal recessive")) {
            sj.add("AR");
        }

        if (omimDiseaseName.contains("Pseudoautosomal dominant")) {
            sj.add("PD");
        }

        if (omimDiseaseName.contains("Pseudoautosomal recessive")) {
            sj.add("PR");
        }

        if (omimDiseaseName.contains("Digenic dominant")) {
            sj.add("DD");
        }

        if (omimDiseaseName.contains("Digenic recessive")) {
            sj.add("DR");
        }

        if (omimDiseaseName.contains("Isolated cases")) {
            sj.add("IC");
        }

        if (omimDiseaseName.contains("Inherited chromosomal imbalance")) {
            sj.add("ICB");
        }

        if (omimDiseaseName.contains("Mitochondrial")) {
            sj.add("Mi");
        }

        if (omimDiseaseName.contains("Multifactorial")) {
            sj.add("Mu");
        }

        if (omimDiseaseName.contains("Somatic mosaicism")) {
            sj.add("SMo");
        }

        if (omimDiseaseName.contains("Somatic mutation")) {
            sj.add("SMu");
        }

        if (omimDiseaseName.contains("X-linked")) {
            sj.add("XL");
        }

        if (omimDiseaseName.contains("X-linked dominant")) {
            sj.add("XLD");
        }

        if (omimDiseaseName.contains("X-linked recessive")) {
            sj.add("XLR");
        }

        if (omimDiseaseName.contains("Y-linked")) {
            sj.add("YL");
        }

        if (sj.length() == 0) {
            return Data.STRING_NA;
        }

        return sj.toString();
    }

    public String getClinGen() {
        return clinGen;
    }

    public boolean isInClinGen() {
        return clinGen.equals("Sufficient evidence")
                || clinGen.equals("Some evidence")
                || clinGen.equals("Recessive evidence");
    }

    public boolean isInClinGenSufficientOrSomeEvidence() {
        return clinGen.equals("Sufficient evidence")
                || clinGen.equals("Some evidence");
    }
    
    public boolean isInClinGenRecessiveEvidence() {
        return clinGen.equals("Recessive evidence");
    }

    public ClinVarPathoratio getClinVarPathoratio() {
        return clinVarPathoratio;
    }

    public boolean isKnownVar2bpFlankingValid() {
        return hgmdOutput.is2bpFlankingValid() || clinVarOutput.isPLP2bpFlankingValid();
    }
    
    public boolean isHGMD2bpFlankingValid() {
        return hgmdOutput.is2bpFlankingValid();
    }
    
    public boolean isClinVar2bpFlankingValid() {
        return clinVarOutput.isPLP2bpFlankingValid();
    }
    
    public boolean isClinVar25bpFlankingValid() {
        return clinVarOutput.isPLP25bpFlankingValid();
    }

    public String getACMG() {
        return acmg;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
