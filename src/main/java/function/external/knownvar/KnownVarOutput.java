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
        
        return true;
    }
    
    // a variant is either HGMD "DM" (not in CinVar) or ClinVar P/LP
    public boolean isKnownVariant() {
        return hgmdOutput.isHGMDDM()
                || clinVarOutput.isClinVarPLP();
    }

    // a variant at the same site is reported HGDM as "DM" or "DM?"
    // a variant at the same site is reported ClinVar as "Pathogenic" or "Likely_pathogenic"
    public boolean hasKnownVariantOnSite() {
        return hgmdOutput.isDMSiteValid() || clinVarOutput.isPLPSiteValid();
    }
    
    public ClinVarPathoratio getClinVarPathoratio() {
        return clinVarPathoratio;
    }
    
    public boolean isKnownVar10bpFlankingValid() {
        return hgmdOutput.is10bpFlankingValid() || clinVarOutput.isPLP10bpFlankingValid();
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
