package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;
import function.annotation.base.GeneManager;
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
    private ClinGen clinGen;
    private String omimDiseaseName;
    private int recessiveCarrier;
    private String acmg;
    private DBDSMOutput dbDSMOutput;

    public static String getHeader() {
        return "Variant ID,"
                + "Gene Name,"
                + KnownVarManager.getHeader();
    }

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        String geneName = GeneManager.getUpToDateGene(annotatedVar.getGeneName()).toUpperCase();
        hgmdOutput = KnownVarManager.getHGMDOutput(annotatedVar);
        clinVarOutput = KnownVarManager.getClinVarOutput(annotatedVar);
        clinVarPathoratio = KnownVarManager.getClinPathoratio(geneName);
        clinGen = KnownVarManager.getClinGen(geneName);
        omimDiseaseName = KnownVarManager.getOMIM(geneName);
//        recessiveCarrier = KnownVarManager.getRecessiveCarrier(geneName);
        acmg = KnownVarManager.getACMG(geneName);
//        dbDSMOutput = KnownVarManager.getDBDSMOutput(annotatedVar);
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(hgmdOutput.getStringJoiner());
        sj.merge(clinVarOutput.getStringJoiner());
        sj.merge(clinVarPathoratio.getStringJoiner());
        sj.merge(clinGen.getStringJoiner());
        sj.add(FormatManager.appendDoubleQuote(omimDiseaseName));
        sj.add(getOMIMInheritance());
//        sj.add(FormatManager.getInteger(recessiveCarrier));
        sj.add(acmg);
//        sj.merge(dbDSMOutput.getStringJoiner());

        return sj;
    }

    public boolean isHGMDDM() {
        return hgmdOutput.getHGMD().getVariantClass().equals("DM");
    }

    public boolean hasHGMDDM() {
        return hgmdOutput.getHGMD().getVariantClass().contains("DM");
    }

    public boolean isClinVarPLP() {
        return clinVarOutput.isClinVarPLP();
    }

    public boolean hasClinVarPLP() {
        return clinVarOutput.getClinVar().getClinSig().contains("Pathogenic")
                || clinVarOutput.getClinVar().getClinSig().contains("Likely_pathogenic");
    }

    public boolean isOMIMGene() {
        return !omimDiseaseName.equals(Data.STRING_NA);
    }

    public boolean isOMIMDominant() {
        return omimDiseaseName.contains("Autosomal dominant")
                || omimDiseaseName.contains("X-linked dominant")
                || omimDiseaseName.contains("Pseudoautosomal dominant")
                || omimDiseaseName.contains("Digenic dominant")
                || omimDiseaseName.contains("Somatic mosaicism")
                || omimDiseaseName.contains("Somatic mutation");
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
        
        if(sj.length() == 0) {
            return Data.STRING_NA;
        }
        
        return sj.toString();
    }
    
    public ClinGen getClinGen() {
        return clinGen;
    }

    public ClinVarPathoratio getClinVarPathoratio() {
        return clinVarPathoratio;
    }

    public boolean isHGMDOrClinVarFlankingValid() {
        return hgmdOutput.is10bpFlankingValid() || clinVarOutput.is10bpFlankingValid();
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
