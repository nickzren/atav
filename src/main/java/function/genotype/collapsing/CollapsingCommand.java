package function.genotype.collapsing;

import function.annotation.base.GeneManager;
import function.genotype.statistics.StatisticsCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
import static utils.CommandManager.checkValueValid;

/**
 *
 * @author nick
 */
public class CollapsingCommand {

    public static boolean isCollapsingSingleVariant = false;
    public static boolean isCollapsingCompHet = false;
    public static boolean isRecessive = false;
    public static String coverageSummaryFile = "";
    public static double maxLooAF = Data.NO_FILTER;
    public static boolean isCollapsingDoLinear = false;
    public static boolean isCollapsingDoLogistic = false;
    public static String regionBoundaryFile = "";
    public static boolean isMannWhitneyTest = false;

    public static void initSingleVarOptions(Iterator<CommandOption> iterator)
            throws Exception { // collapsing dom or rec
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--loo-af":
                case "--max-loo-af":
                    checkValueValid(1, 0, option);
                    maxLooAF = getValidDouble(option);
                    break;
                case "--read-coverage-summary":
                    coverageSummaryFile = getValidPath(option);
                    GeneManager.initCoverageSummary();
                    break;
                case "--covariate":
                    StatisticsCommand.covariateFile = getValidPath(option);
                    isCollapsingDoLogistic = true;
                    break;
                case "--quantitative":
                    StatisticsCommand.quantitativeFile = getValidPath(option);
                    isCollapsingDoLinear = true;
                    break;
                case "--region-boundary":
                    regionBoundaryFile = getValidPath(option);
                    break;
                case "--mann-whitney-test":
                    isMannWhitneyTest = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }

        if (isCollapsingDoLinear) {
            isCollapsingDoLogistic = false;
        }
    }

    public static void initCompHetOptions(Iterator<CommandOption> iterator)
            throws Exception { // collapsing comp het
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--loo-af":
                case "--max-loo-af":
                    checkValueValid(1, 0, option);
                    maxLooAF = getValidDouble(option);
                    break;
                case "--read-coverage-summary":
                    coverageSummaryFile = getValidPath(option);
                    GeneManager.initCoverageSummary();
                    break;
                case "--covariate":
                    StatisticsCommand.covariateFile = getValidPath(option);
                    isCollapsingDoLogistic = true;
                    break;
                case "--quantitative":
                    StatisticsCommand.quantitativeFile = getValidPath(option);
                    isCollapsingDoLinear = true;
                    break;
                case "--mann-whitney-test":
                    isMannWhitneyTest = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }

        if (isCollapsingDoLinear) {
            isCollapsingDoLogistic = false;
        }
    }

    public static boolean isMaxLooAFValid(double value) {
        if (maxLooAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLooAF;
    }
}
