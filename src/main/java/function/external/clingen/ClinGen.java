package function.external.clingen;

/**
 *
 * @author nick
 */
public class ClinGen {

    private String clinGen;

    public ClinGen(String haploinsufficiencyDesc) {
        clinGen = haploinsufficiencyDesc;
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
    
    public boolean isInClinGenSufficientEvidence() {
        return clinGen.equals("Sufficient evidence");
    }
    
    public boolean isInClinGenRecessiveEvidence() {
        return clinGen.equals("Recessive evidence");
    }

    @Override
    public String toString() {
        return clinGen;
    }
}
