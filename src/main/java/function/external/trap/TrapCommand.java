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

    public static boolean isValid(float value, String effect) {
        if (value == Data.FLOAT_NA
                || minTrapScore == Data.NO_FILTER
                || !effect.startsWith("missense_variant")) {
            return true;
        }

        return value >= minTrapScore;
    }
}
