package function.external.bis;

import global.Data;

/**
 *
 * @author nick
 */
public class BisCommand {

    public static boolean isListBis = false;
    public static boolean isIncludeBis = false;

    public static float bisDomainScorePercentile0005 = Data.NO_FILTER;
    public static float bisDomainScorePercentile0001 = Data.NO_FILTER;
    public static float bisDomainScorePercentile00005 = Data.NO_FILTER;
    public static float bisDomainScorePercentile00001 = Data.NO_FILTER;

    public static float bisExonScorePercentile0005 = Data.NO_FILTER;
    public static float bisExonScorePercentile0001 = Data.NO_FILTER;
    public static float bisExonScorePercentile00005 = Data.NO_FILTER;
    public static float bisExonScorePercentile00001 = Data.NO_FILTER;
    
    public static boolean isBisDomainScore0005Valid(float value) {
        if (bisDomainScorePercentile0005 == Data.NO_FILTER) {
            return true;
        }

        return value <= bisDomainScorePercentile0005
                || value == Data.FLOAT_NA;
    }

    public static boolean isBisDomainScore0001Valid(float value) {
        if (bisDomainScorePercentile0001 == Data.NO_FILTER) {
            return true;
        }

        return value <= bisDomainScorePercentile0001
                || value == Data.FLOAT_NA;
    }

    public static boolean isBisDomainScore00005Valid(float value) {
        if (bisDomainScorePercentile00005 == Data.NO_FILTER) {
            return true;
        }

        return value <= bisDomainScorePercentile00005
                || value == Data.FLOAT_NA;
    }

    public static boolean isBisDomainScore00001Valid(float value) {
        if (bisDomainScorePercentile00001 == Data.NO_FILTER) {
            return true;
        }

        return value <= bisDomainScorePercentile00001
                || value == Data.FLOAT_NA;
    }

    public static boolean isBisExonScore0005Valid(float value) {
        if (bisExonScorePercentile0005 == Data.NO_FILTER) {
            return true;
        }

        return value <= bisExonScorePercentile0005
                || value == Data.FLOAT_NA;
    }
    
    public static boolean isBisExonScore0001Valid(float value) {
        if (bisExonScorePercentile0001 == Data.NO_FILTER) {
            return true;
        }

        return value <= bisExonScorePercentile0001
                || value == Data.FLOAT_NA;
    }
    
    public static boolean isBisExonScore00005Valid(float value) {
        if (bisExonScorePercentile00005 == Data.NO_FILTER) {
            return true;
        }

        return value <= bisExonScorePercentile00005
                || value == Data.FLOAT_NA;
    }
    
    public static boolean isBisExonScore00001Valid(float value) {
        if (bisExonScorePercentile00001 == Data.NO_FILTER) {
            return true;
        }

        return value <= bisExonScorePercentile00001
                || value == Data.FLOAT_NA;
    }
}
