package function.external.primateai;

import global.Data;

/**
 *
 * @author nick
 */
public class PrimateAICommand {
    
    public static boolean isListPrimateAI = false;
    public static boolean isIncludePrimateAI = false;

    // filter option
    public static float minPrimateDLScore = Data.NO_FILTER;

    public static boolean isMinPrimateDLScoreValid(float value) {
        if (minPrimateDLScore == Data.NO_FILTER) {
            return true;
        }

        return value >= minPrimateDLScore
                || value == Data.FLOAT_NA;
    }
}
