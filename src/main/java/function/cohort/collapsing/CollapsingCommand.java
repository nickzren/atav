package function.cohort.collapsing;

import function.annotation.base.GeneManager;
import function.cohort.statistics.StatisticsCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class CollapsingCommand {

    public static boolean isCollapsingSingleVariant = false;
    public static boolean isCollapsingCompHet = false;
    public static boolean isCollapsingLite = false;
    public static boolean isCollapsingVCFLite = false;
    public static boolean isRecessive = false;
    public static String coverageSummaryFile = "";
    public static boolean isCollapsingDoLinear = false;
    public static boolean isCollapsingDoLogistic = false;
    public static String regionBoundaryFile = "";
    public static boolean isMannWhitneyTest = false;
    public static int minCompHetVarDistance = Data.NO_FILTER;
    public static String geneColumn = "";
    public static boolean isCollapsingMatrixHetHom = false;

    public static void initSingleVarOptions(Iterator<CommandOption> iterator)
            throws Exception { // collapsing dom or rec
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
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
                case "--convert-nan":
                    Data.STRING_NAN = option.getValue();
                    break;
                case "--collapsing-matrix-het-hom":
                    isCollapsingMatrixHetHom = true;
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
                case "--convert-nan":
                    Data.STRING_NAN = option.getValue();
                    break;
                case "--min-comp-het-var-distance":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCompHetVarDistance = getValidInteger(option);
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

    public static void initLiteOptions(Iterator<CommandOption> iterator)
            throws Exception { // collapsing lite
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--mann-whitney-test":
                    isMannWhitneyTest = true;
                    break;
                case "--read-coverage-summary":
                    coverageSummaryFile = getValidPath(option);
                    GeneManager.initCoverageSummary();
                    break;
                case "--gene-column":
                    geneColumn = option.getValue().replaceAll("_", " ");
                    break;
                case "--collapsing-matrix-het-hom":
                    isCollapsingMatrixHetHom = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }

    public static boolean isMinCompHetVarDistanceValid(int value) {
        if (minCompHetVarDistance == Data.NO_FILTER) {
            return true;
        }

        return value >= minCompHetVarDistance;
    }
}
