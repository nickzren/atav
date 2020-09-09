package function.external.igmaf;

import global.Data;

/**
 *
 * @author nick
 */
public class IGMAFCommand {

    public static boolean isInclude = false;
    public static float maxAF = Data.NO_FILTER;
    public static float maf = Data.NO_FILTER;

    public static boolean isAFValid(float value) {
        return isMaxAFValid(value) && isMAFValid(value);

    }

    private static boolean isMaxAFValid(float value) {
        if (maxAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxAF
                || value == Data.FLOAT_NA;
    }

    private static boolean isMAFValid(float value) {
        if (maf == Data.NO_FILTER) {
            return true;
        }

        return value <= maf
                || value >= (1 - maf)
                || value == Data.FLOAT_NA;
    }
}
