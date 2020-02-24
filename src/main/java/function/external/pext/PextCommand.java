package function.external.pext;

import global.Data;

/**
 *
 * @author nick
 */
public class PextCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;

    public static float minPextRatio = Data.NO_FILTER;

    public static boolean isPextRatioValid(float value) {
        if (minPextRatio == Data.NO_FILTER) {
            return true;
        }

        return value >= minPextRatio;
    }
}
