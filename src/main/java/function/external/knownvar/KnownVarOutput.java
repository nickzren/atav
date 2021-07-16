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
        return omimDiseaseName.contains("[AD]")
                || omimDiseaseName.contains("[XLD]");
    }

    public boolean isOMIMRecessive() {
        return omimDiseaseName.contains("[AR]")
                || omimDiseaseName.contains("[XLR]");
    }

    public String getOMIMInheritance() {
        if (isOMIMDominant()
                && isOMIMRecessive()) {
            return "BOTH";
        } else if (isOMIMDominant()) {
            return "DOMINANT";
        } else if (isOMIMRecessive()) {
            return "RECESSIVE";
        } else {
            return Data.STRING_NA;
        }
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
