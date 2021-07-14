package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;
import function.annotation.base.GeneManager;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    private HGMDOutput hgmdOutput;
    private ClinVarOutput clinVarOutput;
    private ClinVarPathoratio clinVarPathoratio;
//    private int recessiveCarrier;
//    private DBDSMOutput dbDSMOutput;

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
//        recessiveCarrier = KnownVarManager.getRecessiveCarrier(geneName);
//        dbDSMOutput = KnownVarManager.getDBDSMOutput(annotatedVar);
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(hgmdOutput.getStringJoiner());
        sj.merge(clinVarOutput.getStringJoiner());
//        sj.merge(clinVarPathoratio.getStringJoiner());
//        sj.add(FormatManager.getInteger(recessiveCarrier));
//        sj.merge(dbDSMOutput.getStringJoiner());

        return sj;
    }

    public boolean isValid() {
        if (KnownVarCommand.isKnownVarOnly) {
            return this.hgmdOutput.isHGMDVariant() || this.clinVarOutput.isClinVar();
        }

        return true;
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

    public ClinVarPathoratio getClinVarPathoratio() {
        return clinVarPathoratio;
    }
    
    public boolean isHGMDOrClinVarFlankingValid(boolean isSNV) {
        return hgmdOutput.isFlankingValid(isSNV) || clinVarOutput.isFlankingValid(isSNV);
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
