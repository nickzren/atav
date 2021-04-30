package function.cohort.base;

import function.variant.base.VariantManager;
import global.Data;
import global.Index;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class CohortLevelFilterCommand {

    public static String sampleFile = "";
    public static boolean isDisableCheckDuplicateSample = false;
    public static boolean isAllSample = false;
    public static boolean isAllExome = false;
    public static boolean isAvailableControlUseOnly = false;
    public static boolean isExcludeIGMGnomadSample = false;
    public static boolean isExcludeLowQualitySample = false;
    public static boolean isIncludeDefaultControlSample = false;
    public static float maxAF = Data.NO_FILTER;
    public static float minAF = Data.NO_FILTER;
    public static float maxMAF = Data.NO_FILTER;
    public static float minMAF = Data.NO_FILTER;
    public static float maxCtrlAF = Data.NO_FILTER;
    public static float maxCaseAF = Data.NO_FILTER;
    public static float minCtrlAF = Data.NO_FILTER;
    public static float maxCtrlMAF = Data.NO_FILTER;
    public static float maxCaseMAF = Data.NO_FILTER;
    public static int minVarPresent = 1; // special case
    public static int maxAC = Data.NO_FILTER;
    public static int minAC = Data.NO_FILTER;
    public static int minCaseCarrier = Data.NO_FILTER;
    public static int maxQcFailSample = Data.NO_FILTER;
    public static double minCoveredSampleBinomialP = Data.NO_FILTER;
    public static float[] minCoveredSamplePercentage = {Data.NO_FILTER, Data.NO_FILTER};
    public static boolean isCaseOnly = false;
    public static int maxCaseOnlyNumber = 3000;
    public static float maxLooAF = Data.NO_FILTER;
    public static float maxLooMAF = Data.NO_FILTER;

    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--sample":
                    sampleFile = getValidPath(option);
                    break;
                case "--all-sample":
                    isAllSample = true;
                    break;
                case "--all-exome":
                    isAllExome = true;
                    break;
                case "--available-control-use-only":
                    isAvailableControlUseOnly = true;
                    break;
                case "--exclude-igm-gnomad-sample":
                    isExcludeIGMGnomadSample = true;
                    break;
                case "--exclude-low-quality-sample":
                    isExcludeLowQualitySample = true;
                    break;
                case "--include-default-control-sample":
                    isIncludeDefaultControlSample = true;
                    break;
                case "--disable-check-duplicate-sample":
                    isDisableCheckDuplicateSample = true;
                    break;
                case "--max-af":
                    checkValueValid(1, 0, option);
                    maxAF = getValidFloat(option);
                    break;
                case "--min-af":
                    checkValueValid(1, 0, option);
                    minAF = getValidFloat(option);
                    break;
                case "--max-maf":
                    checkValueValid(0.5, 0, option);
                    maxMAF = getValidFloat(option);
                    break;
                case "--min-maf":
                    checkValueValid(0.5, 0, option);
                    minMAF = getValidFloat(option);
                    break;
                case "--ctrl-af":
                case "--max-ctrl-af":
                    checkValueValid(1, 0, option);
                    maxCtrlAF = getValidFloat(option);
                    break;
                case "--min-ctrl-af":
                    checkValueValid(1, 0, option);
                    minCtrlAF = getValidFloat(option);
                    break;
                case "--ctrl-maf":
                case "--max-ctrl-maf":
                    checkValueValid(1, 0, option);
                    maxCtrlMAF = getValidFloat(option);
                    break;
                case "--case-af":
                case "--max-case-af":
                    checkValueValid(1, 0, option);
                    maxCaseAF = getValidFloat(option);
                    break;
                case "--case-maf":
                case "--max-case-maf":
                    checkValueValid(0.5, 0, option);
                    maxCaseMAF = getValidFloat(option);
                    break;
                case "--loo-af":
                case "--max-loo-af":
                    checkValueValid(1, 0, option);
                    maxLooAF = getValidFloat(option);
                    break;
                case "--loo-maf":
                case "--max-loo-maf":
                    checkValueValid(0.5, 0, option);
                    maxLooMAF = getValidFloat(option);
                    break;
                case "--max-ac":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    maxAC = getValidInteger(option);
                    break;
                case "--min-ac":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minAC = getValidInteger(option);
                    break;
                case "--min-variant-present":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minVarPresent = getValidInteger(option);
                    break;
                case "--min-case-carrier":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCaseCarrier = getValidInteger(option);
                    break;
                case "--max-qc-fail-sample":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    maxQcFailSample = getValidInteger(option);
                    break;
                case "--min-covered-sample-binomial-p":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCoveredSampleBinomialP = getValidDouble(option);
                    break;
                case "--min-covered-case-percentage":
                    checkValueValid(100, 0, option);
                    minCoveredSamplePercentage[Index.CASE] = getValidFloat(option);
                    break;
                case "--min-covered-ctrl-percentage":
                    checkValueValid(100, 0, option);
                    minCoveredSamplePercentage[Index.CTRL] = getValidFloat(option);
                    break;
                case "--case-only":
                    isCaseOnly = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }

    public static boolean isAFValid(float value) {
        return isMaxAFValid(value)
                && isMinAFValid(value)
                && isMaxMAFValid(value) 
                && isMinMAFValid(value);
    }

    public static boolean isCtrlAFValid(float value) {
        return isMinCtrlAFValid(value)
                && isMaxCtrlAFValid(value)
                && isMaxCtrlMAFValid(value);
    }

    private static boolean isMinCtrlAFValid(float value) {
        if (minCtrlAF == Data.NO_FILTER) {
            return true;
        }

        return value >= minCtrlAF;
    }

    private static boolean isMaxAFValid(float value) {
        if (maxAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxAF;
    }

    private static boolean isMinAFValid(float value) {
        if (minAF == Data.NO_FILTER) {
            return true;
        }

        return value > minAF;
    }

    private static boolean isMaxMAFValid(float value) {
        if (maxMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMAF || value >= (1 - maxMAF);
    }
    
    private static boolean isMinMAFValid(float value) {
        if (minMAF == Data.NO_FILTER) {
            return true;
        }

        return value > minMAF && value < (1 - minMAF);
    }

    private static boolean isMaxCtrlAFValid(float value) {
        if (maxCtrlAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxCtrlAF;
    }

    private static boolean isMaxCtrlMAFValid(float value) {
        if (maxCtrlMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxCtrlMAF || value >= (1 - maxCtrlMAF);
    }

    public static boolean isCaseAFValid(float value) {
        return isMaxCaseAFValid(value) && isMaxCaseMAFValid(value);
    }

    private static boolean isMaxCaseAFValid(float value) {
        if (maxCaseAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxCaseAF;
    }

    private static boolean isMaxCaseMAFValid(float value) {
        if (maxCaseMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxCaseMAF || value >= (1 - maxCaseMAF);
    }

    public static boolean isMinVarPresentValid(int value) {
        if (minVarPresent == Data.NO_FILTER) {
            return true;
        }

        return value >= minVarPresent;
    }

    public static boolean isACValid(int value) {
        return isMaxACValid(value)
                && isMinACValid(value);
    }

    private static boolean isMaxACValid(int value) {
        if (maxAC == Data.NO_FILTER) {
            return true;
        }

        return value <= maxAC;
    }

    private static boolean isMinACValid(int value) {
        if (minAC == Data.NO_FILTER) {
            return true;
        }

        return value > minAC;
    }

    public static boolean isMinCaseCarrierValid(int value) {
        if (minCaseCarrier == Data.NO_FILTER) {
            return true;
        }

        return value >= minCaseCarrier;
    }

    public static boolean isMaxQcFailSampleValid(int value) {
        if (maxQcFailSample == Data.NO_FILTER) {
            return true;
        }

        return value <= maxQcFailSample;
    }

    public static boolean isMinCoveredSampleBinomialPValid(double value) {
        if (minCoveredSampleBinomialP == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.DOUBLE_NA) {
            return false;
        }

        return value >= minCoveredSampleBinomialP;
    }

    public static boolean isMinCoveredCasePercentageValid(float value) {
        if (minCoveredSamplePercentage[Index.CASE] == Data.NO_FILTER) {
            return true;
        }

        return value >= minCoveredSamplePercentage[Index.CASE];
    }

    public static boolean isMinCoveredCtrlPercentageValid(float value) {
        if (minCoveredSamplePercentage[Index.CTRL] == Data.NO_FILTER) {
            return true;
        }

        return value >= minCoveredSamplePercentage[Index.CTRL];
    }

    public static boolean isCaseOnlyValid2CreateTempTable() {
        // only init case variants tmp tables when 1) total case# < 500 and 2) --variant not used

        return isCaseOnly
                && SampleManager.getCaseNum() > 0
                && SampleManager.getCaseNum() <= maxCaseOnlyNumber
                && !VariantManager.isUsed();
    }

    public static boolean isLooAFValid(float value) {
        return isMaxLooAFValid(value) && isMaxLooMAFValid(value);
    }

    private static boolean isMaxLooAFValid(float value) {
        if (maxLooAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLooAF;
    }

    private static boolean isMaxLooMAFValid(float value) {
        if (maxLooMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLooMAF || value >= (1 - maxLooMAF);
    }
}
