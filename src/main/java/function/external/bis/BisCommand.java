package function.external.bis;

import global.Data;

/**
 *
 * @author nick
 */
public class BisCommand {

    public static boolean isListBis = false;
    public static boolean isIncludeBis = false;

    public static float bisExonPercentile = Data.NO_FILTER;
    
    public static boolean isBisExonPercentileValid(float value) {
        if (bisExonPercentile == Data.NO_FILTER) {
            return true;
        }

        return value <= bisExonPercentile
                || value == Data.FLOAT_NA;
    }
}
