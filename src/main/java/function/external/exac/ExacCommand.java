package function.external.exac;

import global.Data;

/**
 *
 * @author nick
 */
public class ExacCommand {

    public static boolean isListExac = false;
    public static boolean isIncludeExac = false;

    public static String exacPop = "global";
    public static float exacMaf = Data.NO_FILTER;
    public static float exacMeanCoverage = Data.NO_FILTER;
    public static float exacAsRfSnv = Data.NO_FILTER;
    public static float exacAsRfIndel = Data.NO_FILTER;

    public static boolean isExacMafValid(float value) {
        if (exacMaf == Data.NO_FILTER) {
            return true;
        }

        return value <= exacMaf
                || value == Data.FLOAT_NA;
    }

    public static boolean isExacMeanCoverageValid(float value) {
        if (exacMeanCoverage == Data.NO_FILTER) {
            return true;
        }

        return value >= exacMeanCoverage;
    }

    public static boolean isExacAsRfValid(float value, boolean isSnv) {
        if (isSnv) {
            return isExacAsRfSnvValid(value);
        } else {
            return isExacAsRfIndelValid(value);
        }
    }

    private static boolean isExacAsRfSnvValid(float value) {
        if (exacAsRfSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= exacAsRfSnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isExacAsRfIndelValid(float value) {
        if (exacAsRfIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= exacAsRfIndel
                || value == Data.FLOAT_NA;
    }
}
