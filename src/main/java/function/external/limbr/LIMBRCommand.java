package function.external.limbr;

import global.Data;

/**
 *
 * @author nick
 */
public class LIMBRCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;

    public static float maxLimbrDomainPercentile = Data.NO_FILTER;
    public static float maxLimbrExonPercentile = Data.NO_FILTER;
    
    public static boolean isLIMBRDomainPercentileValid(float value) {
        if (maxLimbrDomainPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLimbrDomainPercentile
                || value == Data.FLOAT_NA;
    }
    
    public static boolean isLIMBRExonPercentileValid(float value) {
        if (maxLimbrExonPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLimbrExonPercentile
                || value == Data.FLOAT_NA;
    }
}
