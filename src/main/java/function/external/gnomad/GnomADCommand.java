package function.external.gnomad;

import global.Data;

/**
 *
 * @author nick
 */
public class GnomADCommand {

    public static boolean isListGnomADExome = false;
    public static boolean isListGnomADGenome = false;
    public static boolean isIncludeGnomADExome = false;
    public static boolean isIncludeGnomADGenome = false;

    public static String gnomADExomePop = "global";
    public static String gnomADGenomePop = "global";
    public static float gnomADExomeAF = Data.NO_FILTER;
    public static float gnomADGenomeAF = Data.NO_FILTER;
    public static float gnomADExomeAsRfSnv = Data.NO_FILTER;
    public static float gnomADGenomeAsRfSnv = Data.NO_FILTER;
    public static float gnomADExomeAsRfIndel = Data.NO_FILTER;
    public static float gnomADGenomeAsRfIndel = Data.NO_FILTER;
    public static float gnomADExomeABMedian = Data.NO_FILTER;
    public static float gnomADGenomeABMedian = Data.NO_FILTER;

    public static boolean isGnomADExomeAFValid(float value) {
        if (gnomADExomeAF == Data.NO_FILTER) {
            return true;
        }

        return value <= gnomADExomeAF
                || value == Data.FLOAT_NA;
    }

    public static boolean isGnomADGenomeAFValid(float value) {
        if (gnomADGenomeAF == Data.NO_FILTER) {
            return true;
        }

        return value <= gnomADGenomeAF
                || value == Data.FLOAT_NA;
    }

    public static boolean isGnomADExomeAsRfValid(float value, boolean isSnv) {
        if (isSnv) {
            return isGnomADExomeAsRfSnvValid(value);
        } else {
            return isGnomADExomeAsRfIndelValid(value);
        }
    }

    public static boolean isGnomADGenomeAsRfValid(float value, boolean isSnv) {
        if (isSnv) {
            return isGnomADGenomeAsRfSnvValid(value);
        } else {
            return isGnomADGenomeAsRfIndelValid(value);
        }
    }

    private static boolean isGnomADExomeAsRfSnvValid(float value) {
        if (gnomADExomeAsRfSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADExomeAsRfSnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isGnomADGenomeAsRfSnvValid(float value) {
        if (gnomADGenomeAsRfSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADGenomeAsRfSnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isGnomADExomeAsRfIndelValid(float value) {
        if (gnomADExomeAsRfIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADExomeAsRfIndel
                || value == Data.FLOAT_NA;
    }

    private static boolean isGnomADGenomeAsRfIndelValid(float value) {
        if (gnomADGenomeAsRfIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADGenomeAsRfIndel
                || value == Data.FLOAT_NA;
    }

    public static boolean isGnomADExomeABMedianValid(float value) {
        if (gnomADExomeABMedian == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADExomeABMedian
                || value == Data.FLOAT_NA;
    }

    public static boolean isGnomADGenomeABMedianValid(float value) {
        if (gnomADGenomeABMedian == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADGenomeABMedian
                || value == Data.FLOAT_NA;
    }
}
