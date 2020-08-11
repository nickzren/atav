package function.external.exac;

import global.Data;

/**
 *
 * @author nick
 */
public class ExACCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;
    public static boolean isListCount = false;
    public static boolean isIncludeCount = false;

    public static String pop = "global";
    public static float maxAF = Data.NO_FILTER;
    public static float maf = Data.NO_FILTER;
    public static float vqslodSnv = Data.NO_FILTER;
    public static float vqslodIndel = Data.NO_FILTER;
    public static float meanCoverage = Data.NO_FILTER;
    
    public static boolean isAFValid(float value) {
        return isMaxAFValid(value) && isMAFValid(value);
    }

    private static boolean isMaxAFValid(float value) {
        if (maxAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxAF
                || value == Data.FLOAT_NA;
    }
    
    private static boolean isMAFValid(float value) {
        if (maf == Data.NO_FILTER) {
            return true;
        }

        return value <= maf
                || value >= (1 - maf)
                || value == Data.FLOAT_NA;
    }

    public static boolean isVqslodValid(float value, boolean isSnv) {
        if (isSnv) {
            return isVqslodSnvValid(value);
        } else {
            return isVqslodIndelValid(value);
        }
    }

    private static boolean isVqslodSnvValid(float value) {
        if (vqslodSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= vqslodSnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isVqslodIndelValid(float value) {
        if (vqslodIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= vqslodIndel
                || value == Data.FLOAT_NA;
    }

    public static boolean isMeanCoverageValid(float value) {
        if (meanCoverage == Data.NO_FILTER) {
            return true;
        }

        return value >= meanCoverage;
    }
}
