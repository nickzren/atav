package function.genotype.base;

import function.genotype.collapsing.CollapsingCommand;
import function.genotype.parental.ParentalCommand;
import function.genotype.trio.TrioCommand;
import global.Data;
import utils.CommonCommand;

/**
 *
 * @author nick
 */
public class QualityManager {

    public static boolean isMafValid(double value) {
        if (value <= CommonCommand.maf) {
            return true;
        }

        return false;
    }

    public static boolean isMaf4RecessiveValid(double value) {
        if (CommonCommand.maf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommonCommand.maf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isEvsMafValid(double value) {
        if (CommonCommand.evsMaf == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommonCommand.evsMaf) {
            return true;
        }

        return false;
    }

    public static boolean isEvsMhgf4RecessiveValid(double value) {
        if (CommonCommand.evsMhgf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommonCommand.evsMhgf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isEvsStatusValid(String status) {
        if (CommonCommand.isExcludeEvsQcFailed) {
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
        if (CommonCommand.exacMaf == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommonCommand.exacMaf) {
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
        if (CommonCommand.exacVqslodSnv == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommonCommand.exacVqslodSnv
                || value == Data.NA) {
            return true;
        }

        return false;
    }

    private static boolean isExacVqslodIndelValid(float value) {
        if (CommonCommand.exacVqslodIndel == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommonCommand.exacVqslodIndel
                || value == Data.NA) {
            return true;
        }

        return false;
    }

    public static boolean isMhgf4RecessiveValid(double value) {
        if (CommonCommand.mhgf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommonCommand.mhgf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isCombFreqValid(double value) {
        if (TrioCommand.combFreq == Data.NO_FILTER) {
            return true;
        }

        if (value <= TrioCommand.combFreq) {
            return true;
        }

        return false;
    }

    public static boolean isLooCombFreqValid(double value) {
        if (CollapsingCommand.looCombFreq == Data.NO_FILTER) {
            return true;
        }

        if (value <= CollapsingCommand.looCombFreq) {
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
        if (CommonCommand.minVarPresent == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommonCommand.minVarPresent) {
            return true;
        }

        return false;
    }

    public static boolean isMinCaseCarrierValid(int value) {
        if (CommonCommand.minCaseCarrier == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommonCommand.minCaseCarrier) {
            return true;
        }

        return false;
    }

    public static boolean isMinHomCaseRecValid(int value) {
//        if (CommandValue.minHomCaseRec == Data.NO_FILTER) {
//            return true;
//        }
//
//        if (value >= CommandValue.minHomCaseRec) {
//            return true;
//        }

        return false;
    }

    public static boolean isVarStatusValid(String value) {
        if (CommonCommand.varStatus == null) { // no filter or all
            return true;
        }

        if (value == null) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            for (String str : CommonCommand.varStatus) {
                if (value.equals(str)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isGqValid(float value) {
        if (CommonCommand.genotypeQualGQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommonCommand.genotypeQualGQ) {
                return true;
            }
        }

        return false;
    }

    public static boolean isFsValid(float value) {
        if (CommonCommand.strandBiasFS == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value <= CommonCommand.strandBiasFS) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHapScoreValid(float value) {
        if (CommonCommand.haplotypeScore == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value <= CommonCommand.haplotypeScore) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMqValid(float value) {
        if (CommonCommand.rmsMapQualMQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommonCommand.rmsMapQualMQ) {
                return true;
            }
        }

        return false;
    }

    public static boolean isQdValid(float value) {
        if (CommonCommand.qualByDepthQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommonCommand.qualByDepthQD) {
                return true;
            }
        }

        return false;
    }

    public static boolean isQualValid(float value) {
        if (CommonCommand.qual == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommonCommand.qual) {
                return true;
            }
        }

        return false;
    }

    public static boolean isRprsValid(float value) {
        if (CommonCommand.readPosRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommonCommand.readPosRankSum) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMqrsValid(float value) {
        if (CommonCommand.mapQualRankSum == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommonCommand.mapQualRankSum) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHetPercentAltReadValid(double value) {
        if (CommonCommand.hetPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= CommonCommand.hetPercentAltRead[0]
                    && value <= CommonCommand.hetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHomPercentAltReadValid(double value) {
        if (CommonCommand.homPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= CommonCommand.homPercentAltRead[0]
                    && value <= CommonCommand.homPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCscoreValid(float value) {
        if (value == Data.NA
                || CommonCommand.minCscore == Data.NO_FILTER) {
            return true;
        }

        if (value >= CommonCommand.minCscore) {
            return true;
        }

        return false;
    }

    public static boolean isChildQdValid(float value) {
        if (ParentalCommand.childQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommonCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= ParentalCommand.childQD) {
                return true;
            }
        }

        return false;
    }

    public static boolean isChildHetPercentAltReadValid(double value) {
        if (ParentalCommand.childHetPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= ParentalCommand.childHetPercentAltRead[0]
                    && value <= ParentalCommand.childHetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isChildBinomialValid(double value) {
        if (ParentalCommand.minChildBinomial == Data.NO_FILTER) {
            return true;
        }

        if (value != Data.NA
                && value >= ParentalCommand.minChildBinomial) {
            return true;
        }

        return false;
    }

    public static boolean isParentBinomialValid(double value) {
        if (ParentalCommand.maxParentBinomial == Data.NO_FILTER) {
            return true;
        }

        if (value != Data.NA
                && value < ParentalCommand.maxParentBinomial) {
            return true;
        }

        return false;
    }

    public static boolean isMaxQcFailSampleValid(int value) {
        if (CommonCommand.maxQcFailSample == Data.NO_FILTER) {
            return true;
        }

        if (value <= CommonCommand.maxQcFailSample) {
            return true;
        }

        return false;
    }
}
