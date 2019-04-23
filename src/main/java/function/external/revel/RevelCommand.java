package function.external.revel;

import global.Data;

/**
 *
 * @author nick
 */
public class RevelCommand {

    public static boolean isListRevel = false;
    public static boolean isIncludeRevel = false;

    // filter option
    public static float minRevel = Data.NO_FILTER;

    public static boolean isMinRevelValid(float value) {
        if (minRevel == Data.NO_FILTER) {
            return true;
        }

        return value >= minRevel
                || value == Data.FLOAT_NA;
    }
}
