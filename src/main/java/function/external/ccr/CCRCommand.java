package function.external.ccr;

import global.Data;

/**
 *
 * @author nick
 */
public class CCRCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;

    public static float minCCRPercentile = Data.NO_FILTER;
    
    public static boolean isCCRPercentileValid(float value) {
        if (minCCRPercentile == Data.NO_FILTER) {
            return true;
        }

        return value >= minCCRPercentile || minCCRPercentile == Data.FLOAT_NA;
    }
}
