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
    public static float mtrDomainPercentile = Data.NO_FILTER;
    public static float subRVISExonScorePercentile = Data.NO_FILTER;
    public static float mtrExonPercentile = Data.NO_FILTER;

    public static boolean isSubRVISDomainScoreValid(float value) {
        if (subRVISDomainScorePercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= subRVISDomainScorePercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isMTRDomainPercentileValid(float value) {
        if (mtrDomainPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= mtrDomainPercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isSubRVISExonScoreValid(float value) {
        if (subRVISExonScorePercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= subRVISExonScorePercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isMTRExonPercentileValid(float value) {
        if (mtrExonPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= mtrExonPercentile
                || value == Data.FLOAT_NA;
    }
}
