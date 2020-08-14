package function.external.gme;

import global.Data;

/**
 *
 * @author nick
 */
public class GMECommand {

    public static boolean isList = false;
    public static boolean isInclude = false;
    public static float maxAF = Data.NO_FILTER;
    public static float gmeMAF = Data.NO_FILTER;

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
        if (gmeMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= gmeMAF
                || value >= (1 - gmeMAF)
                || value == Data.FLOAT_NA;
    }
}
