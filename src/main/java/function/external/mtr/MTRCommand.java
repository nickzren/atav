package function.external.mtr;

import global.Data;

/**
 *
 * @author nick
 */
public class MTRCommand {

    public static boolean isListMTR = false;
    public static boolean isIncludeMTR = false;
    public static float maxMTR = Data.NO_FILTER;
    public static float maxMTRFDR = Data.NO_FILTER;
    public static float maxMTRCentile = Data.NO_FILTER;

    public static boolean isMaxMTRValid(float value) {
        if (maxMTR == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMTR
                || value == Data.FLOAT_NA;
    }

    public static boolean isMaxMTRFDRValid(float value) {
        if (maxMTRFDR == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMTRFDR
                || value == Data.FLOAT_NA;
    }

    public static boolean isMaxMTRCentileValid(float value) {
        if (maxMTRCentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMTRCentile
                || value == Data.FLOAT_NA;
    }
}
