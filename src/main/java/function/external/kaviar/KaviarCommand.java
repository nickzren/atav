package function.external.kaviar;

import global.Data;

/**
 *
 * @author nick
 */
public class KaviarCommand {

    public static boolean isListKaviar = false;
    public static float maxKaviarAlleleFreq = Data.NO_FILTER;
    public static int minKaviarAlleleCount = Data.NO_FILTER;

    public static boolean isMaxAlleleFreqValid(float value) {
        if (maxKaviarAlleleFreq == Data.NO_FILTER) {
            return true;
        }

        if (value <= maxKaviarAlleleFreq
                || value == Data.NO_FILTER) {
            return true;
        }

        return false;
    }

    public static boolean isMinAlleleCountValid(int value) {
        if (minKaviarAlleleCount == Data.NO_FILTER) {
            return true;
        }

        if (value >= minKaviarAlleleCount
                && value != Data.NO_FILTER) {
            return true;
        }

        return false;
    }
}
