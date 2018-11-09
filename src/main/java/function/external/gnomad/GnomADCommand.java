package function.external.gnomad;

import global.Data;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author nick
 */
public class GnomADCommand {

    public static boolean isListGnomADExome = false;
    public static boolean isListGnomADGenome = false;
    public static boolean isIncludeGnomADExome = false;
    public static boolean isIncludeGnomADGenome = false;
    
    public static Set<String> gnomADExomePopSet = new HashSet<String>(Arrays.asList("global"));
    
    public static String gnomADGenomePop = "global";
    public static float gnomADExomeAF = Data.NO_FILTER;
    public static float gnomADGenomeAF = Data.NO_FILTER;
    public static float gnomADGenomeAsRfSnv = Data.NO_FILTER;
    public static float gnomADGenomeAsRfIndel = Data.NO_FILTER;
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


    public static boolean isGnomADGenomeAsRfValid(float value, boolean isSnv) {
        if (isSnv) {
            return isGnomADGenomeAsRfSnvValid(value);
        } else {
            return isGnomADGenomeAsRfIndelValid(value);
        }
    }

    private static boolean isGnomADGenomeAsRfSnvValid(float value) {
        if (gnomADGenomeAsRfSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADGenomeAsRfSnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isGnomADGenomeAsRfIndelValid(float value) {
        if (gnomADGenomeAsRfIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADGenomeAsRfIndel
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
