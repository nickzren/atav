package function.external.primateai;

import global.Data;

/**
 *
 * @author nick
 */
public class PrimateAICommand {
    
    public static boolean isList = false;
    public static boolean isInclude = false;

    // filter option
    public static float minPrimateAI = Data.NO_FILTER;

    public static boolean isMinPrimateAIValid(float value) {
        if (minPrimateAI == Data.NO_FILTER) {
            return true;
        }

        return value >= minPrimateAI
                || value == Data.FLOAT_NA;
    }
}
