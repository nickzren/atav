package function.external.pext;

import global.Data;

/**
 *
 * @author nick
 */
public class PextCommand {

    public static boolean isListPext = false;
    public static boolean isIncludePext = false;

    public static float minPextScore = Data.NO_FILTER;

    public static boolean isPextScoreValid(float value) {
        if (minPextScore == Data.NO_FILTER) {
            return true;
        }

        return value >= minPextScore;
    }
}
