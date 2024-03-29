package function.cohort.base;

import global.Data;
import global.Index;
import java.util.Iterator;
import java.util.Stack;
import static utils.CommandManager.checkRangeValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidRange;
import utils.CommandOption;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidPath;

/**
 *
 * @author nick
 */
public class GenotypeLevelFilterCommand {

    public static int minDpBin = Data.NO_FILTER;
    public static int minDp = Data.NO_FILTER;
    public static boolean isIncludeHomRef = false;
    public static int[] filter; // null - no filer 
    public static double[] hetPercentAltRead = null; // {min, max}
    public static double[] homPercentAltRead = null;
    public static double minPercentAltReadBinomialP = Data.NO_FILTER;
    public static double maxPercentAltReadBinomialP = Data.NO_FILTER;
    public static int minAdAlt = Data.NO_FILTER;
    public static int minGQ = Data.NO_FILTER;
    public static float maxSnvSOR = Data.NO_FILTER;
    public static float maxIndelSOR = Data.NO_FILTER;
    public static float maxSnvFS = Data.NO_FILTER;
    public static float maxIndelFS = Data.NO_FILTER;
    public static int minMQ = Data.NO_FILTER;
    public static int minQD = Data.NO_FILTER;
    public static int minQual = Data.NO_FILTER;
    public static float minRPRS = Data.NO_FILTER;
    public static float minMQRS = Data.NO_FILTER;
    public static final String[] FILTER = {"PASS", "LIKELY", "INTERMEDIATE", "FAIL"};
    public static boolean isQcMissingIncluded = false;
    public static String genotypeFile = "";
    public static String vcfFile = "";
    public static boolean isHomOnly = false;
    public static boolean isHetOnly = false;
    public static boolean isExcludeCompHetPIDVariant = false;
    public static boolean isExcludeCompHetHPVariant = false;

    // below variables all true will trigger ATAV only retrive high quality variants
    // QUAL >= 30, MQ >= 40, PASS+LIKELY+INTERMEDIATE, & >= 3 DP or DP Bin
    private static boolean isHighQualityCallVariantOnly = false;

    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--min-coverage":
                case "--min-dp-bin":
                    checkValueValid(new String[]{"10"}, option);
                    minDpBin = getValidInteger(option);
                    break;
                case "--min-dp":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minDp = getValidInteger(option);
                    break;
                case "--include-hom-ref":
                    isIncludeHomRef = true;
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
                case "--min-percent-alt-read-binomial-p":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minPercentAltReadBinomialP = getValidDouble(option);
                    break;
                case "--max-percent-alt-read-binomial-p":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    maxPercentAltReadBinomialP = getValidDouble(option);
                    break;
                case "--min-ad-alt":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    minAdAlt = getValidInteger(option);
                    break;
                case "--gq":
                case "--min-gq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    minGQ = getValidInteger(option);
                    break;
                case "--sor":
                case "--max-sor":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    maxSnvSOR = getValidFloat(option);
                    maxIndelSOR = getValidFloat(option);
                    break;
                case "--snv-sor":
                case "--max-snv-sor":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    maxSnvSOR = getValidFloat(option);
                    break;
                case "--indel-sor":
                case "--max-indel-sor":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    maxIndelSOR = getValidFloat(option);
                    break;
                case "--fs":
                case "--max-fs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    maxSnvFS = getValidFloat(option);
                    maxIndelFS = getValidFloat(option);
                    break;
                case "--snv-fs":
                case "--max-snv-fs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    maxSnvFS = getValidFloat(option);
                    break;
                case "--indel-fs":
                case "--max-indel-fs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    maxIndelFS = getValidFloat(option);
                    break;
                case "--mq":
                case "--min-mq":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    minMQ = getValidInteger(option);
                    break;
                case "--qd":
                case "--min-qd":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    minQD = getValidInteger(option);
                    break;
                case "--qual":
                case "--min-qual":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    minQual = getValidInteger(option);
                    break;
                case "--rprs":
                case "--min-rprs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    minRPRS = getValidFloat(option);
                    break;
                case "--mqrs":
                case "--min-mqrs":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    minMQRS = getValidFloat(option);
                    break;
                case "--include-qc-missing":
                    isQcMissingIncluded = true;
                    break;
                case "--genotype":
                    genotypeFile = getValidPath(option);
                    break;
                case "--vcf":
                    vcfFile = getValidPath(option);
                    break;
                case "--hom-only":
                    isHomOnly = true;
                    break;
                case "--het-only":
                    isHetOnly = true;
                    break;
                case "--exclude-comp-het-pid-variant":
                    isExcludeCompHetPIDVariant = true;
                    break;
                case "--exclude-comp-het-hp-variant":
                    isExcludeCompHetHPVariant = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }

        initIsHighQualityVariantOnly();
    }

    private static void initIsHighQualityVariantOnly() {
        // QUAL >= 30, MQ >= 40, PASS,LIKELY,INTERMEDIATE, & >= 3 DP or DP Bin
        if (minQual != Data.NO_FILTER
                && minMQ != Data.NO_FILTER
                && (minDpBin != Data.NO_FILTER || minDp != Data.NO_FILTER)
                && filter != null) {
            if (minQual >= 30
                    && minMQ >= 40
                    && (minDpBin >= 3
                    || minDp >= 3)) {
                Stack<Integer> stack = new Stack<>();
                stack.add(Enum.FILTER.PASS.getValue());
                stack.add(Enum.FILTER.LIKELY.getValue());
                stack.add(Enum.FILTER.INTERMEDIATE.getValue());
                stack.add(Enum.FILTER.FAIL.getValue());

                for (int filterIndex : filter) {
                    stack.removeElement(filterIndex);
                }

                if (stack.size() == 1
                        && stack.contains(Enum.FILTER.FAIL.getValue())) {
                    isHighQualityCallVariantOnly = true;
                }
            }
        }
    }

    public static boolean isHighQualityCallVariantOnly() {
        return isHighQualityCallVariantOnly;
    }

    public static boolean isMinDpBinValid(short value) {
        if (minDpBin == Data.NO_FILTER) {
            return true;
        }

        return value >= minDpBin;
    }

    public static boolean isMinDpValid(short value) {
        if (minDp == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.SHORT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= minDp) {
            return true;
        }

        return false;
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

    public static boolean isMinAdAltValid(short value) {
        if (minAdAlt == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.SHORT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= minAdAlt) {
            return true;
        }

        return false;
    }

    public static boolean isMinGqValid(byte value) {
        if (minGQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.BYTE_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= minGQ) {
            return true;
        }

        return false;
    }

    public static boolean isMaxSorValid(float value, boolean isSnv) {
        if (isSnv) {
            return isMaxSORValid(value, maxSnvSOR);
        } else {
            return isMaxSORValid(value, maxIndelSOR);
        }
    }

    private static boolean isMaxSORValid(float value, float sor) {
        if (sor == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.FLOAT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value <= sor) {
            return true;
        }

        return false;
    }

    public static boolean isMaxFsValid(float value, boolean isSnv) {
        if (isSnv) {
            return isMaxFsValid(value, maxSnvFS);
        } else {
            return isMaxFsValid(value, maxIndelFS);
        }
    }

    private static boolean isMaxFsValid(float value, float fs) {
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

    public static boolean isMinMqValid(byte value) {
        if (minMQ == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.BYTE_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= minMQ) {
            return true;
        }

        return false;
    }

    public static boolean isMinQdValid(byte value) {
        if (minQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.BYTE_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= minQD) {
            return true;
        }

        return false;
    }

    public static boolean isMinQualValid(int value) {
        if (minQual == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.INTEGER_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= minQual) {
            return true;
        }

        return false;
    }

    public static boolean isMinRprsValid(float value) {
        if (minRPRS == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.FLOAT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= minRPRS) {
            return true;
        }

        return false;
    }

    public static boolean isMinMqrsValid(float value) {
        if (minMQRS == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.FLOAT_NA) {
            if (isQcMissingIncluded) {
                return true;
            }
        } else if (value >= minMQRS) {
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

    public static boolean isMinPercentAltReadBinomialPValid(double value) {
        if (minPercentAltReadBinomialP == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.DOUBLE_NA) {
            return false;
        }

        return value >= minPercentAltReadBinomialP;
    }

    public static boolean isMaxPercentAltReadBinomialPValid(double value) {
        if (maxPercentAltReadBinomialP == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.DOUBLE_NA) {
            return false;
        }

        return value <= maxPercentAltReadBinomialP;
    }

    public static boolean isQualifiedGeno(byte geno) {
        // --hom-only
        if (isHomOnly) {
            return geno == Index.HOM;
        }

        // --het-only
        if (isHetOnly) {
            return geno == Index.HET;
        }

        // --include-hom-ref
        if (isIncludeHomRef && geno == Index.REF) {
            return true;
        }

        // default: hom alt or het is valid 
        return geno == Index.HOM || geno == Index.HET;
    }

    private static boolean isCompHetPIDVariantIdInvalid(int variantId1, int variantId2,
            int pidVariantId1, int pidVariantId2) {
        if (pidVariantId1 == Data.INTEGER_NA && pidVariantId2 == Data.INTEGER_NA) {
            return false;
        }

        return variantId1 == pidVariantId2
                || variantId2 == pidVariantId1
                || pidVariantId1 == pidVariantId2;
    }

    private static boolean isCompHetHPVariantIdInvalid(int variantId1, int variantId2,
            int hpVariantId1, int hpVariantId2) {
        if (hpVariantId1 == Data.INTEGER_NA && hpVariantId2 == Data.INTEGER_NA) {
            return false;
        }

        return variantId1 == hpVariantId2
                || variantId2 == hpVariantId1
                || hpVariantId1 == hpVariantId2;
    }

    public static boolean isCompHetPIDVariantIdInvalid(CalledVariant var1, CalledVariant var2, Sample sample) {
        if (!GenotypeLevelFilterCommand.isExcludeCompHetPIDVariant) {
            return false;
        }

        Carrier carrier1 = var1.getCarrier(sample.getId());
        Carrier carrier2 = var2.getCarrier(sample.getId());

        int pidVariantId1 = carrier1 == null ? Data.INTEGER_NA : carrier1.getPIDVariantId();
        int pidVariantId2 = carrier2 == null ? Data.INTEGER_NA : carrier2.getPIDVariantId();

        return GenotypeLevelFilterCommand.isCompHetPIDVariantIdInvalid(
                var1.getVariantId(), var2.getVariantId(),
                pidVariantId1, pidVariantId2);
    }

    public static boolean isCompHetPIDVariantIdInvalid(
            int variantId1, int variantId2,
            Carrier carrier1, Carrier carrier2) {
        if (!GenotypeLevelFilterCommand.isExcludeCompHetPIDVariant) {
            return false;
        }

        int pidVariantId1 = carrier1 == null ? Data.INTEGER_NA : carrier1.getPIDVariantId();
        int pidVariantId2 = carrier2 == null ? Data.INTEGER_NA : carrier2.getPIDVariantId();

        return GenotypeLevelFilterCommand.isCompHetPIDVariantIdInvalid(
                variantId1, variantId2,
                pidVariantId1, pidVariantId2);
    }

    public static boolean isCompHetHPVariantIdInvalid(CalledVariant var1, CalledVariant var2, Sample sample) {
        if (!GenotypeLevelFilterCommand.isExcludeCompHetHPVariant) {
            return false;
        }

        Carrier carrier1 = var1.getCarrier(sample.getId());
        Carrier carrier2 = var2.getCarrier(sample.getId());

        int hpVariantId1 = carrier1 == null ? Data.INTEGER_NA : carrier1.getHPVariantId();
        int hpVariantId2 = carrier2 == null ? Data.INTEGER_NA : carrier2.getHPVariantId();

        return GenotypeLevelFilterCommand.isCompHetHPVariantIdInvalid(
                var1.getVariantId(), var2.getVariantId(),
                hpVariantId1, hpVariantId2);
    }

    public static boolean isCompHetHPVariantIdInvalid(
            int variantId1, int variantId2,
            Carrier carrier1, Carrier carrier2) {
        if (!GenotypeLevelFilterCommand.isExcludeCompHetHPVariant) {
            return false;
        }

        int hpVariantId1 = carrier1 == null ? Data.INTEGER_NA : carrier1.getHPVariantId();
        int hpVariantId2 = carrier2 == null ? Data.INTEGER_NA : carrier2.getHPVariantId();

        return GenotypeLevelFilterCommand.isCompHetHPVariantIdInvalid(
                variantId1, variantId2,
                hpVariantId1, hpVariantId2);
    }
}
