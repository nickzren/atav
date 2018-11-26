package function.external.trap;

import global.Data;

/**
 *
 * @author nick
 */
public class TrapCommand {

    public static boolean isListTrap = false;
    public static boolean isIncludeTrap = false;

    public static float minTrapScore = Data.NO_FILTER;
    public static float minTrapScoreNonCoding = Data.NO_FILTER;

    public static boolean isTrapScoreValid(float value) {
        if (minTrapScore == Data.NO_FILTER) {
            return true;
        }

        return value >= minTrapScore;
    }
    
    public static boolean isTrapScoreNonCodingValid(float value) {
        if (minTrapScoreNonCoding == Data.NO_FILTER) {
            return true;
        }

        return value >= minTrapScoreNonCoding;
    }
}
