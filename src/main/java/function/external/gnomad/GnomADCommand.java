package function.external.gnomad;

import global.Data;

/**
 *
 * @author nick
 */
public class GnomADCommand {

    public static boolean isListGnomADExome = false;
    public static boolean isIncludeGnomADExome = false;

    public static String gnomADExomePop = "global";
    public static float gnomADExomeMaf = Data.NO_FILTER;
    public static float gnomADExomeAsRfSnv = Data.NO_FILTER;
    public static float gnomADExomeAsRfIndel = Data.NO_FILTER;

    public static boolean isGnomADExomeMafValid(float value) {
        if (gnomADExomeMaf == Data.NO_FILTER) {
            return true;
        }

        return value <= gnomADExomeMaf
                || value == Data.NA;
    }

    public static boolean isGnomADExomeAsRfValid(float value, boolean isSnv) {
        if (isSnv) {
            return isGnomADExomeAsRfSnvValid(value);
        } else {
            return isGnomADExomeAsRfIndelValid(value);
        }
    }

    private static boolean isGnomADExomeAsRfSnvValid(float value) {
        if (gnomADExomeAsRfSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADExomeAsRfSnv
                || value == Data.NA;
    }

    private static boolean isGnomADExomeAsRfIndelValid(float value) {
        if (gnomADExomeAsRfIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADExomeAsRfIndel
                || value == Data.NA;
    }
}