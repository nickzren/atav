package function.external.gnomad;

import function.external.base.VariantAFCommand;
import global.Data;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author nick
 */
public class GnomADCommand extends VariantAFCommand {

    public static boolean isIncludeGeneMetrics = false;

    public Set<String> popSet = new HashSet<String>(Arrays.asList("global"));
    public float rfTpProbabilitySnv = Data.NO_FILTER;
    public float rfTpProbabilityIndel = Data.NO_FILTER;

    public boolean isRfTpProbabilityValid(float value, boolean isSnv) {
        if (isSnv) {
            return isRfTpProbabilitySnvValid(value);
        } else {
            return isRfTpProbabilityIndelValid(value);
        }
    }

    private boolean isRfTpProbabilitySnvValid(float value) {
        if (rfTpProbabilitySnv == Data.NO_FILTER) {
            return true;
        }

        return value >= rfTpProbabilitySnv
                || value == Data.FLOAT_NA;
    }

    private boolean isRfTpProbabilityIndelValid(float value) {
        if (rfTpProbabilityIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= rfTpProbabilityIndel
                || value == Data.FLOAT_NA;
    }
}
