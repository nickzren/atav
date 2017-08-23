package function.genotype.base;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkRangeValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import static utils.CommandManager.getValidRange;
import utils.CommandOption;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.checkValueValid;

/**
 *
 * @author nick
 */
public class GenotypeLevelFilterCommand {

    public static String sampleFile = "";
    public static boolean isDisableCheckDuplicateSample = false;
    public static boolean isAllSample = false;
    public static double maxCtrlAF = Data.NO_FILTER;
    public static double minCtrlAF = Data.NO_FILTER;
    public static int minCoverage = Data.NO_FILTER;
    public static int minGQBin = Data.NO_FILTER;
    public static int minCaseCoverageCall = Data.NO_FILTER;
    public static int minCaseCoverageNoCall = Data.NO_FILTER;
    public static int minCtrlCoverageCall = Data.NO_FILTER;
    public static int minCtrlCoverageNoCall = Data.NO_FILTER;
    public static int minVarPresent = 1; // special case
    public static int minCaseCarrier = Data.NO_FILTER;
    public static int[] filter; // null - no filer 
    public static double[] hetPercentAltRead = null; // {min, max}
    public static double[] homPercentAltRead = null;
    public static int snvGQ = Data.NO_FILTER;
    public static int indelGQ = Data.NO_FILTER;
    public static float snvFS = Data.NO_FILTER;
    public static float indelFS = Data.NO_FILTER;
    public static int snvMQ = Data.NO_FILTER;
    public static int indelMQ = Data.NO_FILTER;
    public static int snvQD = Data.NO_FILTER;
    public static int indelQD = Data.NO_FILTER;
    public static int snvQual = Data.NO_FILTER;
    public static int indelQual = Data.NO_FILTER;
    public static float snvRPRS = Data.NO_FILTER;
    public static float indelRPRS = Data.NO_FILTER;
    public static float snvMQRS = Data.NO_FILTER;
    public static float indelMQRS = Data.NO_FILTER;
    public static final String[] FILTER = {"PASS", "LIKELY", "INTERMEDIATE", "FAIL"};
    public static boolean isQcMissingIncluded = false;
    public static int maxQcFailSample = Data.NO_FILTER;
    public static double minCoveredSampleBinomialP = Data.NO_FILTER;

    // below variables all true will trigger ATAV only retrive high quality variants
    // QUAL >= 30, MQ >= 40, PASS+LIKELY+INTERMEDIATE, & >= 3 DP
    private static boolean isHighQualityCallVariantOnly = false;

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
                case "--min-coverage":
                    checkValueValid(new String[]{"3", "10", "20", "30", "50", "200"}, option);
                    minCoverage = getValidInteger(option);
                    break;
                case "--min-gq-bin":
                    checkValueValid(new String[]{"5", "15", "20", "60"}, option);
                    minGQBin = getValidInteger(option);
                    break;
                case "--min-case-coverage-call":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCaseCoverageCall = getValidInteger(option);
                    break;
                case "--min-case-coverage-no-call":
                    checkValueValid(new String[]{"3", "10", "20", "30", "50", "200"}, option);
                    minCaseCoverageNoCall = getValidInteger(option);
                    break;
                case "--min-ctrl-coverage-call":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCtrlCoverageCall = getValidInteger(option);
                    break;
                case "--min-ctrl-coverage-no-call":
                    checkValueValid(new String[]{"3", "10", "20", "30", "50", "200"}, option);
                    minCtrlCoverageNoCall = getValidInteger(option);
                    break;
                case "--min-variant-present":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minVarPresent = getValidInteger(option);
                    break;
                case "--min-case-carrier":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCaseCarrier = getValidInteger(option);
                    break;
                case "--filter":
                    option.setValue(option.getValue().toUpperCase());
                    checkValuesValid(FILTER, option);
                    String[] tmp = option.getValue().split(",");

                    filter = new int[tmp.length];

                    for (int i = 0; i < tmp.length; i++) {
                        filter[i] = Enum.FILTER.valueOf(tmp[i]).getValue();
                    }
                    break;
                case "--het-percent-alt-read":
                    checkRangeValid("0-1", option);
                    hetPercentAltRead = getValidRange(option);
                    break;
                case "--hom-percent-alt-read":
                    checkRangeValid("0-1", option);
                    homPercentAltRead = getValidRange(option);
                    break;
                case "--gq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvGQ = getValidInteger(option);
                    indelGQ = getValidInteger(option);
                    break;
                case "--snv-gq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvGQ = getValidInteger(option);
                    break;
                case "--indel-gq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    indelGQ = getValidInteger(option);
                    break;
                case "--fs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvFS = getValidFloat(option);
                    indelFS = getValidFloat(option);
                    break;
                case "--snv-fs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvFS = getValidFloat(option);
                    break;
                case "--indel-fs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    indelFS = getValidFloat(option);
                    break;
                case "--mq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvMQ = getValidInteger(option);
                    indelMQ = getValidInteger(option);
                    break;
                case "--snv-mq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvMQ = getValidInteger(option);
                    break;
                case "--indel-mq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    indelMQ = getValidInteger(option);
                    break;
                case "--qd":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvQD = getValidInteger(option);
                    break;
                case "--qual":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvQual = getValidInteger(option);
                    indelQual = getValidInteger(option);
                    break;
                case "--snv-qual":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvQual = getValidInteger(option);
                    break;
                case "--indel-qual":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    indelQual = getValidInteger(option);
                    break;
                case "--rprs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvRPRS = getValidFloat(option);
                    indelRPRS = getValidFloat(option);
                    break;
                case "--snv-rprs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvRPRS = getValidFloat(option);
                    break;
                case "--indel-rprs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    indelRPRS = getValidFloat(option);
                    break;
                case "--mqrs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvMQRS = getValidFloat(option);
                    indelMQRS = getValidFloat(option);
                    break;
                case "--snv-mqrs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    snvMQRS = getValidFloat(option);
                    break;
                case "--indel-mqrs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    indelMQRS = getValidFloat(option);
                    break;
                case "--include-qc-missing":
                    isQcMissingIncluded = true;
                    break;
                case "--max-qc-fail-sample":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    maxQcFailSample = getValidInteger(option);
                    break;
                case "--min-covered-sample-binomial-p":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCoveredSampleBinomialP = getValidDouble(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }

        initMinCoverage();

        initIsHighQualityVariantOnly();
    }

    private static void initMinCoverage() {
        if (minCoverage != Data.NO_FILTER) {
            if (minCaseCoverageCall == Data.NO_FILTER) {
                minCaseCoverageCall = minCoverage;
            }

            if (minCaseCoverageNoCall == Data.NO_FILTER) {
                minCaseCoverageNoCall = minCoverage;
            }

            if (minCtrlCoverageCall == Data.NO_FILTER) {
                minCtrlCoverageCall = minCoverage;
            }

            if (minCtrlCoverageNoCall == Data.NO_FILTER) {
                minCtrlCoverageNoCall = minCoverage;
            }
        }
    }

    private static void initIsHighQualityVariantOnly() {
        // QUAL >= 30, MQ >= 40, PASS,LIKELY,INTERMEDIATE, & >= 3 DP
        if ((snvQual >= 30 & indelQual >= 30)
                && (snvMQ >= 40 && indelMQ >= 40)
                && minCoverage >= 3
                && filter != null) {

            int qualifiedFilterCount = 0;

            for (int filterIndex : filter) {
                if (filterIndex == Enum.FILTER.PASS.getValue()
                        || filterIndex == Enum.FILTER.LIKELY.getValue()
                        || filterIndex == Enum.FILTER.INTERMEDIATE.getValue()) {
                    qualifiedFilterCount++;
                }
            }

            if (qualifiedFilterCount == 3) {
                isHighQualityCallVariantOnly = true;
            }
        }
    }

    public static boolean isHighQualityCallVariantOnly() {
        return isHighQualityCallVariantOnly;
    }

    public static boolean isMaxCtrlAFValid(double value) {
        if (maxCtrlAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxCtrlAF;
    }

    public static boolean isMinCtrlAFValid(double value) {
        if (minCtrlAF == Data.NO_FILTER) {
            return true;
        }

        return value >= minCtrlAF;
    }

    public static boolean isMinCoverageValid(short value, int minCov) {
        if (minCov == Data.NO_FILTER) {
            return true;
        }

        return value >= minCov;
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

    public static boolean isFilterValid(byte value) {
        if (filter == null) { // no filter
            return true;
        }

        for (int tmp : filter) {
            if (value == tmp) {
                return true;
            }
        }

        return false;
    }

    public static boolean isGqValid(byte value, boolean isSnv) {
        if (isSnv) {
            return isGqValid(value, snvGQ);
        } else {
            return isGqValid(value, indelGQ);
        }
    }

    private static boolean isGqValid(byte value, int gq) {
        if (gq == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.BYTE_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= gq) {
            return true;
        }

        return false;
    }

    public static boolean isFsValid(float value, boolean isSnv) {
        if (isSnv) {
            return isFsValid(value, snvFS);
        } else {
            return isFsValid(value, indelFS);
        }
    }

    private static boolean isFsValid(float value, float fs) {
        if (fs == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.FLOAT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value <= fs) {
            return true;
        }

        return false;
    }

    public static boolean isMqValid(byte value, boolean isSnv) {
        if (isSnv) {
            return isMqValid(value, snvMQ);
        } else {
            return isMqValid(value, indelMQ);
        }
    }

    private static boolean isMqValid(byte value, int mq) {
        if (mq == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.BYTE_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= mq) {
            return true;
        }

        return false;
    }

    public static boolean isQdValid(byte value, boolean isSnv) {
        if (isSnv) {
            return isQdValid(value, snvQD);
        } else {
            return isQdValid(value, indelQD);
        }
    }

    private static boolean isQdValid(byte value, int qd) {
        if (qd == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.BYTE_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= qd) {
            return true;
        }

        return false;
    }

    public static boolean isQualValid(int value, boolean isSnv) {
        if (isSnv) {
            return isQualValid(value, snvQual);
        } else {
            return isQualValid(value, indelQual);
        }
    }

    private static boolean isQualValid(int value, int qual) {
        if (qual == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.INTEGER_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= qual) {
            return true;
        }

        return false;
    }

    public static boolean isRprsValid(float value, boolean isSnv) {
        if (isSnv) {
            return isRprsValid(value, snvRPRS);
        } else {
            return isRprsValid(value, indelRPRS);
        }
    }

    private static boolean isRprsValid(float value, float readPosRankSum) {
        if (readPosRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.FLOAT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= readPosRankSum) {
            return true;
        }

        return false;
    }

    public static boolean isMqrsValid(float value, boolean isSnv) {
        if (isSnv) {
            return isMqrsValid(value, snvMQRS);
        } else {
            return isMqrsValid(value, indelMQRS);
        }
    }

    private static boolean isMqrsValid(float value, float mqRankSum) {
        if (mqRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.FLOAT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= mqRankSum) {
            return true;
        }

        return false;
    }

    public static boolean isHetPercentAltReadValid(float value) {
        if (hetPercentAltRead == null) {
            return true;
        }

        if (value != Data.FLOAT_NA) {
            if (value >= hetPercentAltRead[0]
                    && value <= hetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHomPercentAltReadValid(float value) {
        if (homPercentAltRead == null) {
            return true;
        }

        if (value != Data.FLOAT_NA) {
            if (value >= homPercentAltRead[0]
                    && value <= homPercentAltRead[1]) {
                return true;
            }
        }

        return false;
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

        return value >= minCoveredSampleBinomialP;
    }
}
