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
    public static Set<String> gnomADGenomePopSet = new HashSet<String>(Arrays.asList("global"));
    public static float gnomADExomeAF = Data.NO_FILTER;
    public static float gnomADGenomeAF = Data.NO_FILTER;
    public static float gnomADExomeRfTpProbabilitySnv = Data.NO_FILTER;
    public static float gnomADExomeRfTpProbabilityIndel = Data.NO_FILTER;
    public static float gnomADGenomeRfTpProbabilitySnv = Data.NO_FILTER;
    public static float gnomADGenomeRfTpProbabilityIndel = Data.NO_FILTER;

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

    public static boolean isGnomADExomeRfTpProbabilityValid(float value, boolean isSnv) {
        if (isSnv) {
            return isGnomADExomeRfTpProbabilitySnvValid(value);
        } else {
            return isGnomADExomeRfTpProbabilityIndelValid(value);
        }
    }

    public static boolean isGnomADGenomeRfTpProbabilityValid(float value, boolean isSnv) {
        if (isSnv) {
            return isGnomADGenomeRfTpProbabilitySnvValid(value);
        } else {
            return isGnomADGenomeRfTpProbabilityIndelValid(value);
        }
    }

    private static boolean isGnomADExomeRfTpProbabilitySnvValid(float value) {
        if (gnomADExomeRfTpProbabilitySnv == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADExomeRfTpProbabilitySnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isGnomADExomeRfTpProbabilityIndelValid(float value) {
        if (gnomADExomeRfTpProbabilityIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADExomeRfTpProbabilityIndel
                || value == Data.FLOAT_NA;
    }

    private static boolean isGnomADGenomeRfTpProbabilitySnvValid(float value) {
        if (gnomADGenomeRfTpProbabilitySnv == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADGenomeRfTpProbabilitySnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isGnomADGenomeRfTpProbabilityIndelValid(float value) {
        if (gnomADGenomeRfTpProbabilityIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= gnomADGenomeRfTpProbabilityIndel
                || value == Data.FLOAT_NA;
    }
}
