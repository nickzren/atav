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
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidFloat;

/**
 *
 * @author nick
 */
public class GenotypeLevelFilterCommand {

    public static String sampleFile = "";
    public static boolean isAllSample = false;
    public static boolean isAllNonRef = false;
    public static boolean isAllGeno = false;
    public static double maxCtrlMaf = Data.NO_FILTER;
    public static double minCtrlMaf = Data.NO_FILTER;
    public static int minCoverage = Data.NO_FILTER;
    public static int minCaseCoverageCall = Data.NO_FILTER;
    public static int minCaseCoverageNoCall = Data.NO_FILTER;
    public static int minCtrlCoverageCall = Data.NO_FILTER;
    public static int minCtrlCoverageNoCall = Data.NO_FILTER;
    public static int minVarPresent = 1; // special case
    public static int minCaseCarrier = Data.NO_FILTER;
    public static int[] vcfFilter; // null - no filer 
    public static double[] hetPercentAltRead = null; // {min, max}
    public static double[] homPercentAltRead = null;
    public static int genotypeQualGQ = Data.NO_FILTER;
    public static float strandBiasFS = Data.NO_FILTER;
    public static int rmsMapQualMQ = Data.NO_FILTER;
    public static int qualByDepthQD = Data.NO_FILTER;
    public static int qual = Data.NO_FILTER;
    public static float readPosRankSum = Data.NO_FILTER;
    public static float mapQualRankSum = Data.NO_FILTER;
    public static boolean isQcMissingIncluded = false;
    public static int maxQcFailSample = Data.NO_FILTER;

    public static final String[] VCF_FILTER = {"PASS", "LIKELY", "INTERMEDIATE", "FAIL"};

    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--sample":
                case "--pedinfo":
                    sampleFile = getValidPath(option);
                    break;
                case "--all-sample":
                    isAllSample = true;
                    break;
                case "--all-non-ref":
                    isAllNonRef = true;
                    break;
                case "--all-geno":
                    isAllGeno = true;
                    break;
                case "--ctrlMAF":
                case "--ctrl-maf":
                case "--max-ctrl-maf":
                    checkValueValid(0.5, 0, option);
                    maxCtrlMaf = getValidDouble(option);
                    break;
                case "--min-ctrl-maf":
                    checkValueValid(0.5, 0, option);
                    minCtrlMaf = getValidDouble(option);
                    break;
                case "--min-coverage":
                    checkValueValid(new String[]{"0", "3", "10", "20", "201"}, option);
                    minCoverage = getValidInteger(option);
                    break;
                case "--min-case-coverage-call":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCaseCoverageCall = getValidInteger(option);
                    break;
                case "--min-case-coverage-no-call":
                    checkValueValid(new String[]{"3", "10", "20", "201"}, option);
                    minCaseCoverageNoCall = getValidInteger(option);
                    break;
                case "--min-ctrl-coverage-call":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCtrlCoverageCall = getValidInteger(option);
                    break;
                case "--min-ctrl-coverage-no-call":
                    checkValueValid(new String[]{"3", "10", "20", "201"}, option);
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
                case "--var-status":
                case "--vcf-filter":
                    checkValuesValid(VCF_FILTER, option);
                    String[] tmp = option.getValue().split(",");

                    vcfFilter = new int[tmp.length];

                    for (int i = 0; i < tmp.length; i++) {
                        vcfFilter[i] = Enum.FILTER.valueOf(tmp[i]).getValue();
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
                    genotypeQualGQ = getValidInteger(option);
                    break;
                case "--fs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    strandBiasFS = getValidFloat(option);
                    break;
                case "--mq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    rmsMapQualMQ = getValidInteger(option);
                    break;
                case "--qd":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    qualByDepthQD = getValidInteger(option);
                    break;
                case "--qual":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    qual = getValidInteger(option);
                    break;
                case "--rprs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    readPosRankSum = getValidFloat(option);
                    break;
                case "--mqrs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    mapQualRankSum = getValidFloat(option);
                    break;
                case "--include-qc-missing":
                    isQcMissingIncluded = true;
                    break;
                case "--max-qc-fail-sample":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    maxQcFailSample = getValidInteger(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }

        initMinCoverage();
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

    public static boolean isMaxCtrlMafValid(double value) {
        if (maxCtrlMaf == Data.NO_FILTER) {
            return true;
        }

        return value <= maxCtrlMaf;
    }

    public static boolean isMinCtrlMafValid(double value) {
        if (minCtrlMaf == Data.NO_FILTER) {
            return true;
        }

        return value >= minCtrlMaf;
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

    public static boolean isVcfFilterValid(int value) {
        if (vcfFilter == null) { // no filter
            return true;
        }

        for (int tmp : vcfFilter) {
            if (value == tmp) {
                return true;
            }
        }

        return false;
    }

    public static boolean isGqValid(int value) {
        if (genotypeQualGQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.INTEGER_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= genotypeQualGQ) {
            return true;
        }

        return false;
    }

    public static boolean isFsValid(float value) {
        if (strandBiasFS == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.FLOAT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value <= strandBiasFS) {
            return true;
        }

        return false;
    }

    public static boolean isMqValid(int value) {
        if (rmsMapQualMQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.INTEGER_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= rmsMapQualMQ) {
            return true;
        }

        return false;
    }

    public static boolean isQdValid(int value) {
        if (qualByDepthQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.INTEGER_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= qualByDepthQD) {
            return true;
        }

        return false;
    }

    public static boolean isQualValid(int value) {
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

    public static boolean isRprsValid(float value) {
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

    public static boolean isMqrsValid(float value) {
        if (mapQualRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.FLOAT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= mapQualRankSum) {
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
}
