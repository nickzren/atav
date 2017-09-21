package function.external.mtr;

import global.Data;

/**
 *
 * @author nick
 */
public class MTRCommand {

    public static boolean isListMTR = false;
    public static boolean isIncludeMTR = false;
    public static float mtr = Data.NO_FILTER;
    public static float fdr = Data.NO_FILTER;
    public static float mtrCentile = Data.NO_FILTER;

    public static boolean isMTRValid(float value) {
        if (mtr == Data.NO_FILTER) {
            return true;
        }

        return value <= mtr
                || value == Data.NA;
    }

    public static boolean isFDRValid(float value) {
        if (fdr == Data.NO_FILTER) {
            return true;
        }

        return value <= fdr
                || value == Data.NA;
    }

    public static boolean isMTRCentileValid(float value) {
        if (mtrCentile == Data.NO_FILTER) {
            return true;
        }

        return value <= mtrCentile
                || value == Data.NA;
    }
}
