package function.external.subrvis;

import global.Data;

/**
 *
 * @author nick
 */
public class SubRvisCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;

    public static float maxSubRVISDomainPercentile = Data.NO_FILTER;
    public static float maxMtrDomainPercentile = Data.NO_FILTER;
    public static float maxSubRVISExonPercentile = Data.NO_FILTER;
    public static float maxMtrExonPercentile = Data.NO_FILTER;

    public static boolean isSubRVISDomainPercentileValid(float value) {
        if (maxSubRVISDomainPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxSubRVISDomainPercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isMTRDomainPercentileValid(float value) {
        if (maxMtrDomainPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMtrDomainPercentile
                || value == Data.FLOAT_NA;
    }

    public static boolean isSubRVISExonPercentileValid(float value) {
        if (maxSubRVISExonPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxSubRVISExonPercentile
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
