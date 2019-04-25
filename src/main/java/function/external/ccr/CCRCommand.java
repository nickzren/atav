package function.external.ccr;

import global.Data;

/**
 *
 * @author nick
 */
public class CCRCommand {

    public static boolean isListCCR = false;
    public static boolean isIncludeCCR = false;

    public static float ccrPercentile = Data.NO_FILTER;
    
    public static boolean isCCRPercentileValid(float value) {
        if (ccrPercentile == Data.NO_FILTER) {
            return true;
        }

        return value >= ccrPercentile;
    }
}
