package function.external.limbr;

import global.Data;

/**
 *
 * @author nick
 */
public class LIMBRCommand {

    public static boolean isListLIMBR = false;
    public static boolean isIncludeLIMBR = false;

    public static float limbrDomainPercentile = Data.NO_FILTER;
    public static float limbrExonPercentile = Data.NO_FILTER;
    
    public static boolean isLIMBRDomainPercentileValid(float value) {
        if (limbrDomainPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= limbrDomainPercentile
                || value == Data.FLOAT_NA;
    }
    
    public static boolean isLIMBRExonPercentileValid(float value) {
        if (limbrExonPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= limbrExonPercentile
                || value == Data.FLOAT_NA;
    }
}
