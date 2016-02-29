package function.external.knownvar;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinGen {

    private String haploinsufficiencyDesc;
    private String triplosensitivityDesc;

    public ClinGen(String haploinsufficiencyDesc, String triplosensitivityDesc) {
        this.haploinsufficiencyDesc = haploinsufficiencyDesc;
        this.triplosensitivityDesc = triplosensitivityDesc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getString(haploinsufficiencyDesc)).append(",");
        sb.append(FormatManager.getString(triplosensitivityDesc)).append(",");

        return sb.toString();
    }
}
