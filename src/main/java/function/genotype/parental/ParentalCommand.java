package function.genotype.parental;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkRangeValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidRange;
import utils.CommandOption;
import utils.CommonCommand;

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
            if (option.getName().equals("--child-qd")) {
                childQD = getValidDouble(option);
            } else if (option.getName().equals("--child-het-percent-alt-read")) {
                checkRangeValid("0-1", option);
                childHetPercentAltRead = getValidRange(option);
            } else if (option.getName().equals("--min-child-binomial")) {
                minChildBinomial = getValidDouble(option);
            } else if (option.getName().equals("--max-parent-binomial")) {
                maxParentBinomial = getValidDouble(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }
}
