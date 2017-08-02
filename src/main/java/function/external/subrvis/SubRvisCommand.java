package function.external.subrvis;

import global.Data;

/**
 *
 * @author nick
 */
public class SubRvisCommand {

    public static boolean isListSubRvis = false;
    public static boolean isIncludeSubRvis = false;

    public static float subRVISDomainScorePercentile = Data.NO_FILTER;
    public static float subRVISDomainOEratioPercentile = Data.NO_FILTER;
    public static float subRVISExonScorePercentile = Data.NO_FILTER;
    public static float subRVISExonOEratioPercentile = Data.NO_FILTER;

    public static boolean isSubRVISDomainScoreValid(float value) {
        if (subRVISDomainScorePercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= subRVISDomainScorePercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isSubRVISDomainOEratioValid(float value) {
        if (subRVISDomainOEratioPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= subRVISDomainOEratioPercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isSubRVISExonScoreValid(float value) {
        if (subRVISExonScorePercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= subRVISExonScorePercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isSubRVISExonOEratioValid(float value) {
        if (subRVISExonOEratioPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= subRVISExonOEratioPercentile
                || value == Data.FLOAT_NA;
    }
}
