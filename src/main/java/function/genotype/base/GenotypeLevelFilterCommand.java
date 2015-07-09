package function.genotype.base;

import function.genotype.collapsing.CollapsingCommand;
import function.genotype.family.FamilyCommand;
import function.variant.base.VariantLevelFilterCommand;
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
    public static String evsSample = "";
    public static double maf = 0.5;
    public static double ctrlMaf = 0.5;
    public static double maf4Recessive = Data.NO_FILTER;
    public static double mhgf4Recessive = Data.NO_FILTER;
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

    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--sample")
                    || option.getName().equals("--pedinfo")) {
                sampleFile = getValidPath(option);
            } else if (option.getName().equals("--all-sample")) {
                isAllSample = true;
            } else if (option.getName().equals("--all-non-ref")) {
                isAllNonRef = true;
            } else if (option.getName().equals("--include-evs-sample")) {
                checkValueValid(Data.EVS_POP, option);
                evsSample = option.getValue();
            } else if (option.getName().equals("--exclude-artifacts")) {
                VariantLevelFilterCommand.isExcludeArtifacts = true;
            } else if (option.getName().equals("--ctrlMAF")
                    || option.getName().equals("--ctrl-maf")) {
                checkValueValid(0.5, 0, option);
                ctrlMaf = getValidDouble(option);
            } else if (option.getName().equals("--ctrl-maf-rec")) {
                maf4Recessive = getValidDouble(option);
                checkValueValid(0.5, 0, option);
            } else if (option.getName().equals("--ctrl-mhgf-rec")) {
                mhgf4Recessive = getValidDouble(option);
                checkValueValid(0.5, 0, option);
            } else if (option.getName().equals("--min-coverage")) {
                checkValueValid(new String[]{"0", "3", "10", "20", "201"}, option);
                minCoverage = getValidInteger(option);
            } else if (option.getName().equals("--min-case-coverage-call")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                minCaseCoverageCall = getValidInteger(option);
            } else if (option.getName().equals("--min-case-coverage-no-call")) {
                checkValueValid(new String[]{"3", "10", "20", "201"}, option);
                minCaseCoverageNoCall = getValidInteger(option);
            } else if (option.getName().equals("--min-ctrl-coverage-call")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                minCtrlCoverageCall = getValidInteger(option);
            } else if (option.getName().equals("--min-ctrl-coverage-no-call")) {
                checkValueValid(new String[]{"3", "10", "20", "201"}, option);
                minCtrlCoverageNoCall = getValidInteger(option);
            } else if (option.getName().equals("--min-variant-present")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                minVarPresent = getValidInteger(option);
            } else if (option.getName().equals("--min-case-carrier")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                minCaseCarrier = getValidInteger(option);
            } else if (option.getName().equals("--var-status")) {
                checkValueValid(Data.VARIANT_STATUS, option);
                String str = option.getValue().replace("+", ",");
                if (str.contains("all")) {
                    varStatus = null;
                } else {
                    varStatus = str.split(",");
                }
            } else if (option.getName().equals("--het-percent-alt-read")) {
                checkRangeValid("0-1", option);
                hetPercentAltRead = getValidRange(option);
            } else if (option.getName().equals("--hom-percent-alt-read")) {
                checkRangeValid("0-1", option);
                homPercentAltRead = getValidRange(option);
            } else if (option.getName().equals("--gq")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                genotypeQualGQ = getValidDouble(option);
            } else if (option.getName().equals("--fs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                strandBiasFS = getValidDouble(option);
            } else if (option.getName().equals("--hap-score")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                haplotypeScore = getValidDouble(option);
            } else if (option.getName().equals("--mq")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                rmsMapQualMQ = getValidDouble(option);
            } else if (option.getName().equals("--qd")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                qualByDepthQD = getValidDouble(option);
            } else if (option.getName().equals("--qual")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                qual = getValidDouble(option);
            } else if (option.getName().equals("--rprs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                readPosRankSum = getValidDouble(option);
            } else if (option.getName().equals("--mqrs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                mapQualRankSum = getValidDouble(option);
            } else if (option.getName().equals("--include-qc-missing")) {
                isQcMissingIncluded = true;
            } else if (option.getName().equals("--max-qc-fail-sample")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                maxQcFailSample = getValidInteger(option);
            } else {
                continue;
            }

            iterator.remove();
        }

        initMinCoverage();

        initMaf();
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

    private static void initMaf() {
        if (CollapsingCommand.looMaf != Data.NO_FILTER) {
            maf = CollapsingCommand.looMaf;
        } else if (FamilyCommand.popCtrlMaf != Data.NO_FILTER) {
            maf = FamilyCommand.popCtrlMaf;
        } else {
            maf = ctrlMaf;
        }

        if (maf4Recessive == Data.NO_FILTER) { // need to be changed for pop
            maf4Recessive = maf;
        }
    }

    public static boolean isMafValid(double value) {
        if (value <= maf) {
            return true;
        }

        return false;
    }

    public static boolean isMaf4RecessiveValid(double value) {
        if (maf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (value <= maf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isMhgf4RecessiveValid(double value) {
        if (mhgf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (value <= mhgf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isMinCoverageValid(int value, int minCov) {
        if (minCov == Data.NO_FILTER) {
            return true;
        }

        if (value >= minCov) {
            return true;
        }

        return false;
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

        if (value >= minCaseCarrier) {
            return true;
        }

        return false;
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

        if (value <= maxQcFailSample) {
            return true;
        }

        return false;
    }
}
