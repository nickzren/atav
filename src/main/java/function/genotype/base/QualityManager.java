package function.genotype.base;

import global.Data;
import utils.CommandValue;

/**
 *
 * @author nick
 */
public class QualityManager {

    public static boolean isMafValid(double value) {
        if (CommandValue.isFlipMaf) {
            if (value >= CommandValue.maf) {
                return true;
            }
        } else {
            if (value <= CommandValue.maf) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMaf4RecessiveValid(double value) {
        if (CommandValue.maf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (CommandValue.isFlipMaf) {
            if (value >= CommandValue.maf) {
                return true;
            }
        } else if (value <= CommandValue.maf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isEvsMafValid(double value) {
        if (CommandValue.evsMaf == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommandValue.evsMaf) {
            return true;
        }

        return false;
    }

    public static boolean isEvsMhgf4RecessiveValid(double value) {
        if (CommandValue.evsMhgf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommandValue.evsMhgf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isEvsStatusValid(String status) {
        if (CommandValue.isExcludeEvsQcFailed) {
            if (status.equalsIgnoreCase("NA")
                    || status.equalsIgnoreCase("pass")) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean isExacMafValid(float value) {
        if (CommandValue.exacMaf == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommandValue.exacMaf) {
            return true;
        }

        return false;
    }

    public static boolean isExacVqslodValid(float value, boolean isSnv) {
        if (isSnv) {
            return isExacVqslodSnvValid(value);
        } else {
            return isExacVqslodIndelValid(value);
        }
    }

    private static boolean isExacVqslodSnvValid(float value) {
        if (CommandValue.exacVqslodSnv == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommandValue.exacVqslodSnv
                || value == Data.NA) {
            return true;
        }

        return false;
    }

    private static boolean isExacVqslodIndelValid(float value) {
        if (CommandValue.exacVqslodIndel == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommandValue.exacVqslodIndel
                || value == Data.NA) {
            return true;
        }

        return false;
    }

    public static boolean isMhgf4RecessiveValid(double value) {
        if (CommandValue.mhgf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommandValue.mhgf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isCombFreqValid(double value) {
        if (CommandValue.combFreq == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommandValue.combFreq) {
            return true;
        }

        return false;
    }

    public static boolean isLooCombFreqValid(double value) {
        if (CommandValue.looCombFreq == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommandValue.looCombFreq) {
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
        if (CommandValue.minVarPresent == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommandValue.minVarPresent) {
            return true;
        }

        return false;
    }

    public static boolean isMinCaseCarrierValid(int value) {
        if (CommandValue.minCaseCarrier == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommandValue.minCaseCarrier) {
            return true;
        }

        return false;
    }

    public static boolean isMinHomCaseRecValid(int value) {
        if (CommandValue.minHomCaseRec == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommandValue.minHomCaseRec) {
            return true;
        }

        return false;
    }

    public static boolean isVarStatusValid(String value) {
        if (CommandValue.varStatus == null) { // no filter or all
            return true;
        }

        if (value == null) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            for (String str : CommandValue.varStatus) {
                if (value.equals(str)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isGqValid(float value) {
        if (CommandValue.genotypeQualGQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.genotypeQualGQ) {
                return true;
            }
        }

        return false;
    }

    public static boolean isFsValid(float value) {
        if (CommandValue.strandBiasFS == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value <= CommandValue.strandBiasFS) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHapScoreValid(float value) {
        if (CommandValue.haplotypeScore == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value <= CommandValue.haplotypeScore) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMqValid(float value) {
        if (CommandValue.rmsMapQualMQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.rmsMapQualMQ) {
                return true;
            }
        }

        return false;
    }

    public static boolean isQdValid(float value) {
        if (CommandValue.qualByDepthQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.qualByDepthQD) {
                return true;
            }
        }

        return false;
    }

    public static boolean isQualValid(float value) {
        if (CommandValue.qual == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.qual) {
                return true;
            }
        }

        return false;
    }

    public static boolean isRprsValid(float value) {
        if (CommandValue.readPosRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.readPosRankSum) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMqrsValid(float value) {
        if (CommandValue.mapQualRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.mapQualRankSum) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHetPercentAltReadValid(double value) {
        if (CommandValue.hetPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= CommandValue.hetPercentAltRead[0]
                    && value <= CommandValue.hetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHomPercentAltReadValid(double value) {
        if (CommandValue.homPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= CommandValue.homPercentAltRead[0]
                    && value <= CommandValue.homPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCscoreValid(float value) {
        if (value == Data.NA
                || CommandValue.minCscore == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommandValue.minCscore) {
            return true;
        }

        return false;
    }

    public static boolean isChildQdValid(float value) {
        if (CommandValue.childQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.childQD) {
                return true;
            }
        }

        return false;
    }

    public static boolean isChildHetPercentAltReadValid(double value) {
        if (CommandValue.childHetPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= CommandValue.childHetPercentAltRead[0]
                    && value <= CommandValue.childHetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isChildBinomialValid(double value) {
        if (CommandValue.minChildBinomial == Data.NO_FILTER) {
            return true;
        }

        if (value != Data.NA
                && value >= CommandValue.minChildBinomial) {
            return true;
        }

        return false;
    }

    public static boolean isParentBinomialValid(double value) {
        if (CommandValue.maxParentBinomial == Data.NO_FILTER) {
            return true;
        }

        if (value != Data.NA
                && value < CommandValue.maxParentBinomial) {
            return true;
        }

        return false;
    }
}
