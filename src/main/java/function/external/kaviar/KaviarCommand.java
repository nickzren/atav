package function.external.kaviar;

import global.Data;

/**
 *
 * @author nick
 */
public class KaviarCommand {

    public static boolean isListKaviar = false;
    public static float maxKaviarAlleleFreq = Data.NO_FILTER;
    public static int maxKaviarAlleleCount = Data.NO_FILTER;

    public static boolean isMaxAlleleFreqValid(float value) {
        if (maxKaviarAlleleFreq == Data.NO_FILTER) {
            return true;
        }

        if (value <= maxKaviarAlleleFreq
                || value == Data.NA) {
            return true;
        }

        return false;
    }

    public static boolean isMaxAlleleCountValid(int value) {
        if (maxKaviarAlleleCount == Data.NO_FILTER) {
            return true;
        }

        if (value <= maxKaviarAlleleCount
                || value == Data.NA) {
            return true;
        }

        return false;
    }
}
