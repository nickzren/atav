package function.genotype.collapsing;

import function.annotation.base.GeneManager;
import function.genotype.statistics.StatisticsCommand;
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
public class CollapsingCommand {

    public static boolean isCollapsingSingleVariant = false;
    public static boolean isCollapsingCompHet = false;
    public static boolean isRecessive = false;
    public static String coverageSummaryFile = "";
    public static double maxLooMaf = Data.NO_FILTER;
    public static double maxLooMafRec = Data.NO_FILTER;
    public static double maxLooCombFreq = Data.NO_FILTER;
    public static boolean isCollapsingDoLinear = false;
    public static boolean isCollapsingDoLogistic = false;
    public static String regionBoundaryFile = "";

    public static void initSingleVarOptions(Iterator<CommandOption> iterator)
            throws Exception { // collapsing dom or rec
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--loo-maf":
                case "--max-loo-maf":
                    checkValueValid(0.5, 0, option);
                    maxLooMaf = getValidDouble(option);
                    break;
                case "--loo-maf-rec":
                case "--loo-maf-recessive":
                case "--max-loo-maf-rec":
                    checkValueValid(0.5, 0, option);
                    maxLooMafRec = getValidDouble(option);
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
                case "--loo-maf":
                case "--max-loo-maf":
                    checkValueValid(0.5, 0, option);
                    maxLooMaf = getValidDouble(option);
                    break;
                case "--loo-maf-rec":
                case "--loo-maf-recessive":
                case "--max-loo-maf-rec":
                    checkValueValid(0.5, 0, option);
                    maxLooMafRec = getValidDouble(option);
                    break;
                case "--loo-comb-freq":
                case "--max-loo-comb-freq":
                    checkValueValid(1, 0, option);
                    maxLooCombFreq = getValidDouble(option);
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
                default:
                    continue;
            }

            iterator.remove();
        }

        if (isCollapsingDoLinear) {
            isCollapsingDoLogistic = false;
        }
    }

    public static boolean isMaxLooMafValid(double value) {
        if (maxLooMaf == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLooMaf;
    }

    public static boolean isMaxLooMafRecValid(double value) {
        if (maxLooMafRec == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLooMafRec;
    }

    public static boolean isMaxLooCombFreqValid(double value) {
        if (maxLooCombFreq == Data.NO_FILTER) {
            return true;
        }

        return value <= maxLooCombFreq;
    }
}
