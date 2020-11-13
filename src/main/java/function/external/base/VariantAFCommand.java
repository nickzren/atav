package function.external.base;

import global.Data;

/**
 *
 * @author nick
 */
public class VariantAFCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;
    public static float maxAF = Data.NO_FILTER;
    public static float minAF = Data.NO_FILTER;
    public static float maxMAF = Data.NO_FILTER;
    public static float minMAF = Data.NO_FILTER;

    public static boolean isAFValid(float value) {
        return isMaxAFValid(value)
                && isMinAFValid(value)
                && isMaxMAFValid(value)
                && isMinMAFValid(value);

    }

    public static boolean isAFValid(float max, float min) {
        return isMaxAFValid(max)
                && isMinAFValid(min)
                && VariantAFCommand.isMaxMAFValid(max, min)
                && isMinMAFValid(max, min);

    }

    private static boolean isMaxAFValid(float value) {
        if (maxAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxAF
                || value == Data.FLOAT_NA;
    }

    private static boolean isMinAFValid(float value) {
        if (minAF == Data.NO_FILTER) {
            return true;
        }

        return value > minAF;
    }

    private static boolean isMaxMAFValid(float value) {
        if (maxMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMAF
                || value >= (1 - maxMAF)
                || value == Data.FLOAT_NA;
    }

    private static boolean isMinMAFValid(float value) {
        if (minMAF == Data.NO_FILTER) {
            return true;
        }

        return value > minMAF
                && value < (1 - minMAF);
    }

    private static boolean isMaxMAFValid(float max, float min) {
        if (maxMAF == Data.NO_FILTER) {
            return true;
        }
        // maxAF <= cutoff or minAF >= 1 - cutoff
        return max <= maxMAF
                || min >= (1 - maxMAF)
                || max == Data.FLOAT_NA
                || min == Data.FLOAT_NA;
    }

    private static boolean isMinMAFValid(float max, float min) {
        if (minMAF == Data.NO_FILTER) {
            return true;
        }

        // maxAF > cutoff and minAF < 1- cutoff
        return max > minMAF
                && min < (1 - minMAF);
    }
}
