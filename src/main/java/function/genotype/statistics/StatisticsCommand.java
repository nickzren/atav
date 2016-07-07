package function.genotype.statistics;

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
    public static boolean isLogistic = false;
    public static double threshold4Sort = Data.NO_FILTER;
    public static String[] models = {"allelic", "dominant", "recessive", "genotypic"}; // default is fisher models
    public static String[] logisticModels = {"dominant", "recessive","additive" }; //Use LogisticOutput qualifiers
    public static boolean isCaseOnly = false;
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
                case "--case-only":
                    isCaseOnly = true;
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

    public static void initLogisticOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--covariate")) {
                covariateFile = getValidPath(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }
}
