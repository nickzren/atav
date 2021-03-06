package function.external.gerp;

import global.Data;

/**
 *
 * @author nick
 */
public class GerpCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;

    public static float minGerpScore = Data.NO_FILTER;

    public static boolean isGerpScoreValid(float value) {
        if (value == Data.FLOAT_NA
                || minGerpScore == Data.NO_FILTER) {
            return true;
        }

        return value >= minGerpScore;
    }
}
