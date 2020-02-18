package function.external.gme;

import global.Data;

/**
 *
 * @author nick
 */
public class GMECommand {
    public static boolean isListGME = false;
    public static boolean isIncludeGME = false;
    public static float maxGmeAF = Data.NO_FILTER;
    
    public static boolean isMaxGMEAFValid(float value) {
        if (maxGmeAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxGmeAF
                || value == Data.FLOAT_NA;
    }
}
