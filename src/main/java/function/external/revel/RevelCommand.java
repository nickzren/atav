package function.external.revel;

import global.Data;

/**
 *
 * @author nick
 */
public class RevelCommand {

    // list revel
    public static boolean isListRevel = false;
    public static boolean isIncludeRevel = false;

    // filter option
    public static float revel = Data.NO_FILTER;

    public static boolean isRevelValid(float value) {
        if (revel == Data.NO_FILTER) {
            return true;
        }

        return value >= revel
                || value == Data.FLOAT_NA;
    }
}
