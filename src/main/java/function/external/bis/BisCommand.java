package function.external.bis;

import global.Data;

/**
 *
 * @author nick
 */
public class BisCommand {
    public static boolean isListBis = false;
    public static boolean isIncludeBis = false;
    
    public static float BisDomainScorePercentile = Data.NO_FILTER;
    public static float BisExonScorePercentile = Data.NO_FILTER;
    
    public static boolean isBisDomainScoreValid(float value) {
        if (BisDomainScorePercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= BisDomainScorePercentile
                || value == Data.NA;
    }
    
    public static boolean isBisExonScoreValid(float value) {
        if (BisExonScorePercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= BisExonScorePercentile
                || value == Data.NA;
    }
}
