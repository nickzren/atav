package function.external.evs;

import global.Data;

/**
 *
 * @author nick
 */
public class EvsCommand {

    // list evs
    public static boolean isList = false;
    public static boolean isInclude = false;

    // filter option
    public static String evsPop = "all";
    public static double evsMaf = Data.NO_FILTER;
    public static boolean isExcludeEvsQcFailed = false;

    public static boolean isEvsMafValid(float value) {
        if (evsMaf == Data.NO_FILTER) {
            return true;
        }

        return value <= evsMaf
                || value == Data.FLOAT_NA;
    }

    public static boolean isEvsStatusValid(String status) {
        if (isExcludeEvsQcFailed) {
            return status.equalsIgnoreCase(Data.STRING_NA)
                    || status.equalsIgnoreCase("pass");
        } else {
            return true;
        }
    }
}
