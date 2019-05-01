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
    public static boolean isExcludeIGMGnomadSample = false;
    public static double maxCtrlAF = Data.NO_FILTER;
    public static double maxCaseAF = Data.NO_FILTER;
    public static double minCtrlAF = Data.NO_FILTER;
    public static int minVarPresent = 1; // special case
    public static int minCaseCarrier = Data.NO_FILTER;
    public static int maxQcFailSample = Data.NO_FILTER;
    public static double minCoveredSampleBinomialP = Data.NO_FILTER;
    public static float[] minCoveredSamplePercentage = {Data.NO_FILTER, Data.NO_FILTER};
    public static boolean isCaseOnly = false;
    public static int maxCaseOnlyNumber = 3000;
    public static double maxLooAF = Data.NO_FILTER;

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
                case "--exclude-igm-gnomad-sample":
                    isExcludeIGMGnomadSample = true;
                    break;
                case "--disable-check-duplicate-sample":
                    isDisableCheckDuplicateSample = true;
                    break;
                case "--ctrl-af":
                case "--max-ctrl-af":
                    checkValueValid(1, 0, option);
                    maxCtrlAF = getValidDouble(option);
                    break;
                case "--min-ctrl-af":
                    checkValueValid(1, 0, option);
                    minCtrlAF = getValidDouble(option);
                    break;
                case "--case-af":
                case "--max-case-af":
                    checkValueValid(1, 0, option);
                    maxCaseAF = getValidDouble(option);
                    break;
                case "--loo-af":
                case "--max-loo-af":
                    checkValueValid(1, 0, option);
                    maxLooAF = getValidDouble(option);
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

    public static boolean isMaxCaseAFValid(double value) {
        if (maxCaseAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxCaseAF;
    }

    public static boolean isMinCtrlAFValid(double value) {
        if (minCtrlAF == Data.NO_FILTER) {
            return true;
        }

        return value >= minCtrlAF;
    }

    public static boolean isMaxCtrlAFValid(double value) {
        if (maxCtrlAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxCtrlAF;
    }

    public static boolean isMinVarPresentValid(int value) {
        if (minVarPresent == Data.NO_FILTER) {
            return true;
        }

        return value >= minVarPresent;
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

    public static boolean isMaxLooAFValid(double value) {
        if (maxLooAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLooAF;
    }
}
