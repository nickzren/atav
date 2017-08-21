package function.external.discovehr;

import global.Data;

/**
 *
 * @author nick
 */
public class DiscovEHRCommand {

    public static boolean isListDiscovEHR = false;
    public static boolean isIncludeDiscovEHR = false;
    public static float discovEHRAF = Data.NO_FILTER;

    public static boolean isDiscovEHRAFValid(float value) {
        if (discovEHRAF == Data.NO_FILTER) {
            return true;
        }

        return value <= discovEHRAF
                || value == Data.FLOAT_NA;
    }
}
