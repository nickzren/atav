package function.external.gerp;

import global.Data;

/**
 *
 * @author nick
 */
public class GerpCommand {

    public static boolean isListGerp = false;
    public static boolean isIncludeGerp = false;

    public static float minGerpScore = Data.NO_FILTER;

    public static boolean isGerpScoreValid(float value) {
        if (value == Data.NA
                || minGerpScore == Data.NO_FILTER) {
            return true;
        }

        if (value >= minGerpScore) {
            return true;
        }

        return false;
    }
}
