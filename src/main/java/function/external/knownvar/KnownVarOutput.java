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
        recessiveCarrier = KnownVarManager.getRecessiveCarrier(geneName);
        acmg = KnownVarManager.getACMG(geneName);
        dbDSMOutput = KnownVarManager.getDBDSMOutput(annotatedVar);
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(hgmdOutput.getStringJoiner());
        sj.merge(clinVarOutput.getStringJoiner());
        sj.merge(clinVarPathoratio.getStringJoiner());
        sj.merge(clinGen.getStringJoiner());
        sj.add(FormatManager.appendDoubleQuote(omimDiseaseName));
        sj.add(FormatManager.getInteger(recessiveCarrier));
        sj.add(acmg);
        sj.merge(dbDSMOutput.getStringJoiner());

        return sj;
    }

    public boolean isHGMDDM() {
        return hgmdOutput.getHGMD().getVariantClass().equals("DM");
    }
    
    public boolean hasHGMDDM() {
        return hgmdOutput.getHGMD().getVariantClass().contains("DM");
    }
    
    public boolean hasIndel9bpFlanksInHGMD() {
        return hgmdOutput.hasIndel9bpFlanks();
    }
    
    public boolean isClinVarPLP() {
        return clinVarOutput.getClinVar().getClinSig().equals("Pathogenic")
                || clinVarOutput.getClinVar().getClinSig().equals("Likely_pathogenic");
    }
    
    public boolean hasClinVarPLP() {
        return clinVarOutput.getClinVar().getClinSig().contains("Pathogenic")
                || clinVarOutput.getClinVar().getClinSig().contains("Likely_pathogenic");
    }
    
    public boolean isOMIMGene() {
        return !omimDiseaseName.equals(Data.STRING_NA);
    }
    
    public ClinGen getClinGen() {
        return clinGen;
    }
    
    public ClinVarPathoratio getClinVarPathoratio() {
        return clinVarPathoratio;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
