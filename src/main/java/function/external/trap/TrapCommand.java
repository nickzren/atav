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

    public static boolean isTrapScoreValid(float value) {
        if (value == Data.FLOAT_NA
                || minTrapScore == Data.NO_FILTER) {
            return true;
        }

        return value >= minTrapScore;
    }
}
