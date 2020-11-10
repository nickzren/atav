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

    public static Set<String> popSet = new HashSet<String>(Arrays.asList("global"));
    public static float rfTpProbabilitySnv = Data.NO_FILTER;
    public static float rfTpProbabilityIndel = Data.NO_FILTER;

    public static boolean isRfTpProbabilityValid(float value, boolean isSnv) {
        if (isSnv) {
            return isRfTpProbabilitySnvValid(value);
        } else {
            return isRfTpProbabilityIndelValid(value);
        }
    }

    private static boolean isRfTpProbabilitySnvValid(float value) {
        if (rfTpProbabilitySnv == Data.NO_FILTER) {
            return true;
        }

        return value >= rfTpProbabilitySnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isRfTpProbabilityIndelValid(float value) {
        if (rfTpProbabilityIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= rfTpProbabilityIndel
                || value == Data.FLOAT_NA;
    }
}
