package function.external.mpc;

import global.Data;

/**
 *
 * @author nick
 */
public class MPCCommand {
    public static boolean isList = false;
    public static boolean isInclude = false;

    public static float minMPC = Data.NO_FILTER;

    public static boolean isMPCValid(float value) {
        if (value == Data.FLOAT_NA
                || minMPC == Data.NO_FILTER) {
            return true;
        }

        return value >= minMPC;
    }
}
