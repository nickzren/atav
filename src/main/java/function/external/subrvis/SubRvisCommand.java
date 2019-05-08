package function.external.subrvis;

import global.Data;

/**
 *
 * @author nick
 */
public class SubRvisCommand {

    public static boolean isListSubRvis = false;
    public static boolean isIncludeSubRvis = false;

    public static float maxSubRVISDomainScorePercentile = Data.NO_FILTER;
    public static float maxMtrDomainPercentile = Data.NO_FILTER;
    public static float maxSubRVISExonScorePercentile = Data.NO_FILTER;
    public static float maxMtrExonPercentile = Data.NO_FILTER;

    public static boolean isSubRVISDomainScoreValid(float value) {
        if (maxSubRVISDomainScorePercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxSubRVISDomainScorePercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isMTRDomainPercentileValid(float value) {
        if (maxMtrDomainPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMtrDomainPercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isSubRVISExonScoreValid(float value) {
        if (maxSubRVISExonScorePercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxSubRVISExonScorePercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isMTRExonPercentileValid(float value) {
        if (maxMtrExonPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMtrExonPercentile
                || value == Data.FLOAT_NA;
    }
}
