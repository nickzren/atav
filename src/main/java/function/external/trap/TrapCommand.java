package function.external.trap;

import global.Data;

/**
 *
 * @author nick
 */
public class TrapCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;

    public static float minTrapScore = Data.NO_FILTER;
    public static float minTrapScoreNonCoding = Data.NO_FILTER;

    public static boolean isTrapScoreValid(float value) {
        if (value == Data.FLOAT_NA
                || minTrapScore == Data.NO_FILTER) {
            return true;
        }

        return value >= minTrapScore;
    }
    
    public static boolean isTrapScoreNonCodingValid(float value) {
        if (value == Data.FLOAT_NA
                || minTrapScoreNonCoding == Data.NO_FILTER) {
            return true;
        }

        return value >= minTrapScoreNonCoding;
    }
}
