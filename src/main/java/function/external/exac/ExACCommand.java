package function.external.exac;

import function.external.base.VariantAFCommand;
import global.Data;

/**
 *
 * @author nick
 */
public class ExACCommand extends VariantAFCommand {

    public static boolean isListCount = false;
    public static boolean isIncludeCount = false;

    public static String pop = "global";
    public static float vqslodSnv = Data.NO_FILTER;
    public static float vqslodIndel = Data.NO_FILTER;
    public static float meanCoverage = Data.NO_FILTER;

    public static boolean isVqslodValid(float value, boolean isSnv) {
        if (isSnv) {
            return isVqslodSnvValid(value);
        } else {
            return isVqslodIndelValid(value);
        }
    }

    private static boolean isVqslodSnvValid(float value) {
        if (vqslodSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= vqslodSnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isVqslodIndelValid(float value) {
        if (vqslodIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= vqslodIndel
                || value == Data.FLOAT_NA;
    }

    public static boolean isMeanCoverageValid(float value) {
        if (meanCoverage == Data.NO_FILTER) {
            return true;
        }

        return value >= meanCoverage;
    }
}
