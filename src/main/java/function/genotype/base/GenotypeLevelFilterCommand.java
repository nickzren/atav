package function.genotype.base;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkRangeValid;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import static utils.CommandManager.getValidRange;
import utils.CommandOption;

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
    public static String[] varStatus; // null: no filer or all    
    public static double[] hetPercentAltRead = null; // {min, max}
    public static double[] homPercentAltRead = null;
    public static double genotypeQualGQ = Data.NO_FILTER;
    public static double strandBiasFS = Data.NO_FILTER;
    public static double haplotypeScore = Data.NO_FILTER;
    public static double rmsMapQualMQ = Data.NO_FILTER;
    public static double qualByDepthQD = Data.NO_FILTER;
    public static double qual = Data.NO_FILTER;
    public static double readPosRankSum = Data.NO_FILTER;
    public static double mapQualRankSum = Data.NO_FILTER;
    public static boolean isQcMissingIncluded = false;
    public static int maxQcFailSample = Data.NO_FILTER;
    
     public static final String[] VARIANT_STATUS = {"pass", "pass+intermediate", "all"};

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
                    checkValueValid(VARIANT_STATUS, option);
                    String str = option.getValue().replace("+", ",");
                    if (str.contains("all")) {
                        varStatus = null;
                    } else {
                        varStatus = str.split(",");
                    }   break;
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
                    genotypeQualGQ = getValidDouble(option);
                    break;
                case "--fs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    strandBiasFS = getValidDouble(option);
                    break;
                case "--hap-score":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    haplotypeScore = getValidDouble(option);
                    break;
                case "--mq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    rmsMapQualMQ = getValidDouble(option);
                    break;
                case "--qd":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    qualByDepthQD = getValidDouble(option);
                    break;
                case "--qual":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    qual = getValidDouble(option);
                    break;
                case "--rprs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    readPosRankSum = getValidDouble(option);
                    break;
                case "--mqrs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    mapQualRankSum = getValidDouble(option);
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

    public static boolean isMinCoverageValid(int value, int minCov) {
        if (minCov == Data.NO_FILTER) {
            return true;
        }

        return value >= minCov;
    }

    public static boolean isMinVarPresentValid(int value) {
        if (minVarPresent == Data.NO_FILTER) {
            return true;
        }

        if (value >= minVarPresent) {
            return true;
        }

        return false;
    }

    public static boolean isMinCaseCarrierValid(int value) {
        if (minCaseCarrier == Data.NO_FILTER) {
            return true;
        }

        return value >= minCaseCarrier;
    }

    public static boolean isVarStatusValid(String value) {
        if (varStatus == null) { // no filter or all
            return true;
        }

        if (value == null) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            for (String str : varStatus) {
                if (value.equals(str)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isGqValid(float value) {
        if (genotypeQualGQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= genotypeQualGQ) {
                return true;
            }
        }

        return false;
    }

    public static boolean isFsValid(float value) {
        if (strandBiasFS == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value <= strandBiasFS) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHapScoreValid(float value) {
        if (haplotypeScore == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value <= haplotypeScore) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMqValid(float value) {
        if (rmsMapQualMQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= rmsMapQualMQ) {
                return true;
            }
        }

        return false;
    }

    public static boolean isQdValid(float value) {
        if (qualByDepthQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= qualByDepthQD) {
                return true;
            }
        }

        return false;
    }

    public static boolean isQualValid(float value) {
        if (qual == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= qual) {
                return true;
            }
        }

        return false;
    }

    public static boolean isRprsValid(float value) {
        if (readPosRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= readPosRankSum) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMqrsValid(float value) {
        if (mapQualRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= mapQualRankSum) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHetPercentAltReadValid(double value) {
        if (hetPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= hetPercentAltRead[0]
                    && value <= hetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHomPercentAltReadValid(double value) {
        if (homPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
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
