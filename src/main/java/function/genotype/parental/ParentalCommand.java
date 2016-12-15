package function.genotype.parental;

import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkRangeValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidRange;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class ParentalCommand {

    public static boolean isParentalMosaic = false;
    public static double childQD = Data.NO_FILTER;
    public static double[] childHetPercentAltRead = null;
    public static double minChildBinomial = Data.NO_FILTER;
    public static double maxParentBinomial = Data.NO_FILTER;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--child-qd":
                    childQD = getValidDouble(option);
                    break;
                case "--child-het-percent-alt-read":
                    checkRangeValid("0-1", option);
                    childHetPercentAltRead = getValidRange(option);
                    break;
                case "--min-child-binomial":
                    minChildBinomial = getValidDouble(option);
                    break;
                case "--max-parent-binomial":
                    maxParentBinomial = getValidDouble(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }

    public static boolean isChildQdValid(byte value) {
        if (childQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.BYTE_NA) {
            if (GenotypeLevelFilterCommand.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= childQD) {
                return true;
            }
        }

        return false;
    }

    public static boolean isChildHetPercentAltReadValid(double value) {
        if (childHetPercentAltRead == null) {
            return true;
        }

        if (value != Data.DOUBLE_NA) {
            if (value >= childHetPercentAltRead[0]
                    && value <= childHetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    public static boolean isChildBinomialValid(double value) {
        if (minChildBinomial == Data.NO_FILTER) {
            return true;
        }

        return value != Data.DOUBLE_NA
                && value >= minChildBinomial;
    }

    public static boolean isParentBinomialValid(double value) {
        if (maxParentBinomial == Data.NO_FILTER) {
            return true;
        }

        return value != Data.DOUBLE_NA
                && value < maxParentBinomial;
    }
}
