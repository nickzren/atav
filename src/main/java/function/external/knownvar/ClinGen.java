package function.external.knownvar;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinGen {

    String haploinsufficiencyDesc;
    String triplosensitivityDesc;

    public ClinGen(String haploinsufficiencyDesc, String triplosensitivityDesc) {
        this.haploinsufficiencyDesc = haploinsufficiencyDesc;
        this.triplosensitivityDesc = triplosensitivityDesc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getString(haploinsufficiencyDesc)).append(",");
        sb.append(FormatManager.getString(triplosensitivityDesc));

        return sb.toString();
    }
}
