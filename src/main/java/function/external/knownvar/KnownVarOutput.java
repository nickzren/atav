package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;
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
        hgmdOutput = KnownVarManager.getHGMDOutput(annotatedVar);
        clinVarOutput = KnownVarManager.getClinVarOutput(annotatedVar);
//        recessiveCarrier = KnownVarManager.getRecessiveCarrier(geneName);
//        dbDSMOutput = KnownVarManager.getDBDSMOutput(annotatedVar);
        initClinPathoratio(annotatedVar.getGeneName());
    }
    
    public void initClinPathoratio(String geneName) {
        clinVarPathoratio = KnownVarManager.getClinPathoratio(geneName.toUpperCase());
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(hgmdOutput.getStringJoiner());
        sj.merge(clinVarOutput.getStringJoiner());
        sj.merge(clinVarPathoratio.getStringJoiner());
//        sj.add(FormatManager.getInteger(recessiveCarrier));
//        sj.merge(dbDSMOutput.getStringJoiner());

        return sj;
    }

    public boolean isValid() {
        if (KnownVarCommand.isKnownVarOnly) {
            return hgmdOutput.isHGMD() || clinVarOutput.isClinVar();
        }

        if (KnownVarCommand.isKnownVarPathogenicOnly) {
            return hgmdOutput.isHGMDDM() || clinVarOutput.isClinVarPLP();
        }
        
        if (KnownVarCommand.isExcludeClinVarBLB) {
            return !clinVarOutput.isClinVarBLB();
        }
        
        return true;
    }
    
    // a variant is either HGMD "DM" (not CinVar B/LB) or ClinVar P/LP
    public boolean isKnownVariant() {
        return hgmdOutput.isHGMDDM()
                || clinVarOutput.isClinVarPLP();
    }

    // a site has variant is either HGMD "DM" (not CinVar B/LB) or ClinVar P/LP
    public boolean isKnownVariantSite() {
        return hgmdOutput.isHGMDDMSite() 
                || clinVarOutput.isClinVarPLPSite();
    }
    
    public ClinVarPathoratio getClinVarPathoratio() {
        return clinVarPathoratio;
    }
    
    public boolean isKnownVar2bpFlankingValid() {
        return hgmdOutput.is2bpFlankingValid() || clinVarOutput.isPLP2bpFlankingValid();
    }
    
    public boolean isClinVar25bpFlankingValid() {
        return clinVarOutput.isPLP25bpFlankingValid();
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
