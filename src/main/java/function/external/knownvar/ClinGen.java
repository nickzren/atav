package function.external.knownvar;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinGen {

    private int clinGen;
    private String haploinsufficiencyDesc;
    private String triplosensitivityDesc;

    public ClinGen(String haploinsufficiencyDesc, String triplosensitivityDesc) {
        this.haploinsufficiencyDesc = haploinsufficiencyDesc;
        this.triplosensitivityDesc = triplosensitivityDesc;

        if (haploinsufficiencyDesc.equals("Sufficient evidence") ||
                haploinsufficiencyDesc.equals("Some evidence") ||
                haploinsufficiencyDesc.equals("Recessive evidence")) {
            clinGen = 1;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(clinGen).append(",");
        sb.append(FormatManager.getString(haploinsufficiencyDesc)).append(",");
        sb.append(FormatManager.getString(triplosensitivityDesc)).append(",");

        return sb.toString();
    }
}
