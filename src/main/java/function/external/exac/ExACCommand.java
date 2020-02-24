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

    public static String exacPop = "global";
    public static float exacAF = Data.NO_FILTER;
    public static float exacVqslodSnv = Data.NO_FILTER;
    public static float exacVqslodIndel = Data.NO_FILTER;
    public static float exacMeanCoverage = Data.NO_FILTER;

    public static boolean isExacAFValid(float value) {
        if (exacAF == Data.NO_FILTER) {
            return true;
        }

        return value <= exacAF
                || value == Data.FLOAT_NA;
    }

    public static boolean isExacVqslodValid(float value, boolean isSnv) {
        if (isSnv) {
            return isExacVqslodSnvValid(value);
        } else {
            return isExacVqslodIndelValid(value);
        }
    }

    private static boolean isExacVqslodSnvValid(float value) {
        if (exacVqslodSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= exacVqslodSnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isExacVqslodIndelValid(float value) {
        if (exacVqslodIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= exacVqslodIndel
                || value == Data.FLOAT_NA;
    }

    public static boolean isExacMeanCoverageValid(float value) {
        if (exacMeanCoverage == Data.NO_FILTER) {
            return true;
        }

        return value >= exacMeanCoverage;
    }
}
