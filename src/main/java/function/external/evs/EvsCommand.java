package function.external.evs;

import global.Data;

/**
 *
 * @author nick
 */
public class EvsCommand {

    // list evs
    public static boolean isListEvs = false;

    // filter option
    public static String evsMafPop = "all";
    public static double evsMaf = Data.NO_FILTER;
    public static int evsAllAverageCoverage = Data.NO_FILTER;
    public static boolean isExcludeEvsQcFailed = false;

    public static boolean isEvsMafValid(double value) {
        if (evsMaf == Data.NO_FILTER) {
            return true;
        }

        if (value <= evsMaf
                || value == Data.NA) {
            return true;
        }

        return false;
    }

    public static boolean isEvsAllCoverageValid(int value) {
        if (evsAllAverageCoverage == Data.NO_FILTER) {
            return true;
        }

        if (value >= evsAllAverageCoverage) {
            return true;
        }

        return false;
    }

    public static boolean isEvsStatusValid(String status) {
        if (isExcludeEvsQcFailed) {
            if (status.equalsIgnoreCase("NA")
                    || status.equalsIgnoreCase("pass")) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
