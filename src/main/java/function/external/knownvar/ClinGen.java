package function.external.knownvar;

import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinGen {

    private int clinGen;
    private String haploinsufficiencyDesc;
//    private String triplosensitivityDesc;

    public ClinGen(String haploinsufficiencyDesc, String triplosensitivityDesc) {
        this.haploinsufficiencyDesc = haploinsufficiencyDesc;
//        this.triplosensitivityDesc = triplosensitivityDesc;

        if (haploinsufficiencyDesc.equals("Sufficient evidence")
                || haploinsufficiencyDesc.equals("Some evidence")
                || haploinsufficiencyDesc.equals("Recessive evidence")) {
            clinGen = 1;
        }
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(clinGen));
        sj.add(FormatManager.getString(haploinsufficiencyDesc));
//        sj.add(FormatManager.getString(triplosensitivityDesc));

        return sj;
    }

    public boolean isInClinGen() {
        return clinGen == 1;
    }

    public boolean isInClinGenSufficientOrSomeEvidence() {
        return haploinsufficiencyDesc.equals("Sufficient evidence")
                || haploinsufficiencyDesc.equals("Some evidence");
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
