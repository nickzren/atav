package function.external.gme;

import global.Data;

/**
 *
 * @author nick
 */
public class GMECommand {
    public static boolean isList = false;
    public static boolean isInclude = false;
    public static float maxGmeAF = Data.NO_FILTER;
    
    public static boolean isMaxGMEAFValid(float value) {
        if (maxGmeAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxGmeAF
                || value == Data.FLOAT_NA;
    }
}
