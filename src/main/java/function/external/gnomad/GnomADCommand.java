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

    public static boolean isListExome = false;
    public static boolean isListGenome = false;
    public static boolean isIncludeExome = false;
    public static boolean isIncludeGenome = false;
    public static boolean isIncludeGeneMetrics = false;

    public static Set<String> exomePopSet = new HashSet<String>(Arrays.asList("global"));
    public static Set<String> genomePopSet = new HashSet<String>(Arrays.asList("global"));
    public static float maxExomeAF = Data.NO_FILTER;
    public static float maxGenomeAF = Data.NO_FILTER;
    public static float exomeMAF = Data.NO_FILTER;
    public static float genomeMAF = Data.NO_FILTER;
    public static float exomeRfTpProbabilitySnv = Data.NO_FILTER;
    public static float exomeRfTpProbabilityIndel = Data.NO_FILTER;
    public static float genomeRfTpProbabilitySnv = Data.NO_FILTER;
    public static float genomeRfTpProbabilityIndel = Data.NO_FILTER;

    public static boolean isExomeAFValid(float value) {
        return isMaxExomeAFValid(value) && isExomeMAFValid(value);
    }

    private static boolean isMaxExomeAFValid(float value) {
        if (maxExomeAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxExomeAF
                || value == Data.FLOAT_NA;
    }

    private static boolean isExomeMAFValid(float value) {
        if (exomeMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= exomeMAF
                || value >= (1 - exomeMAF)
                || value == Data.FLOAT_NA;
    }

    public static boolean isGenomeAFValid(float value) {
        return isMaxGenomeAFValid(value) && isGenomeMAFValid(value);
    }
    
    private static boolean isMaxGenomeAFValid(float value) {
        if (maxGenomeAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxGenomeAF
                || value == Data.FLOAT_NA;
    }

    private static boolean isGenomeMAFValid(float value) {
        if (genomeMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= genomeMAF
                || value >= (1 - genomeMAF)
                || value == Data.FLOAT_NA;
    }

    public static boolean isExomeRfTpProbabilityValid(float value, boolean isSnv) {
        if (isSnv) {
            return isExomeRfTpProbabilitySnvValid(value);
        } else {
            return isExomeRfTpProbabilityIndelValid(value);
        }
    }

    public static boolean isGenomeRfTpProbabilityValid(float value, boolean isSnv) {
        if (isSnv) {
            return isGenomeRfTpProbabilitySnvValid(value);
        } else {
            return isGenomeRfTpProbabilityIndelValid(value);
        }
    }

    private static boolean isExomeRfTpProbabilitySnvValid(float value) {
        if (exomeRfTpProbabilitySnv == Data.NO_FILTER) {
            return true;
        }

        return value >= exomeRfTpProbabilitySnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isExomeRfTpProbabilityIndelValid(float value) {
        if (exomeRfTpProbabilityIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= exomeRfTpProbabilityIndel
                || value == Data.FLOAT_NA;
    }

    private static boolean isGenomeRfTpProbabilitySnvValid(float value) {
        if (genomeRfTpProbabilitySnv == Data.NO_FILTER) {
            return true;
        }

        return value >= genomeRfTpProbabilitySnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isGenomeRfTpProbabilityIndelValid(float value) {
        if (genomeRfTpProbabilityIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= genomeRfTpProbabilityIndel
                || value == Data.FLOAT_NA;
    }
}
