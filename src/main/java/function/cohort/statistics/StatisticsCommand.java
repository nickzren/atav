package function.cohort.statistics;

import global.Data;

import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class StatisticsCommand {

    public static boolean isFisher = false;
    public static boolean isLinear = false;
    public static double threshold4Sort = Data.NO_FILTER;
    public static String[] fisherModels = {"allelic", "dominant", "recessive", "genotypic"};
    public static String[] linearModels = {"allelic", "dominant", "recessive", "additive"};
    public static String covariateFile = "";
    public static String quantitativeFile = "";

    public static void initFisherOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--threshold-sort":
                    threshold4Sort = getValidDouble(option);
                    checkValueValid(1, 0, option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }

    public static void initLinearOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--threshold-sort":
                    threshold4Sort = getValidDouble(option);
                    checkValueValid(1, 0, option);
                    break;
                case "--covariate":
                    covariateFile = getValidPath(option);
                    break;
                case "--quantitative":
                    quantitativeFile = getValidPath(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
