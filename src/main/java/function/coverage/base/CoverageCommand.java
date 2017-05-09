package function.coverage.base;

import function.genotype.statistics.StatisticsCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class CoverageCommand {

    // coverage summary
    public static boolean isCoverageSummary = false;
    public static boolean isSiteCoverageSummary = false;
    public static double minPercentRegionCovered = 0; //so all is output by default
    public static float exonCleanCutoff = Data.NO_FILTER;
    public static float geneCleanCutoff = 1;
    public static float siteCleanCutoff = Data.NO_FILTER;

    // coverage comparison 
    public static boolean isCoverageComparison = false;
    public static boolean isSiteCoverageComparison = false;
    public static boolean isRelativeDifference = false;
    public static boolean isLinear = false;
    public static float minCoverageFraction = Data.NO_FILTER;

    public static void initCoverageComparison(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--exon-max-percent-cov-difference":
                    checkValueValid(1, 0, option);
                    exonCleanCutoff = getValidFloat(option);
                    break;
                case "--quantitative":
                    isLinear = true;
                    StatisticsCommand.quantitativeFile = getValidPath(option);
                    break;
                case "--relative-difference":
                    isRelativeDifference = true;
                    break;
                case "--min-coverage-fraction":
                    checkValueValid(1, 0, option);
                    minCoverageFraction = getValidFloat(option);
                    break;
                default:
                    continue;
            }
            iterator.remove();
        }
    }

    public static void initCoverageSummary(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--percent-region-covered")) {
                minPercentRegionCovered = getValidFloat(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    public static void initCoverageComparisonSite(Iterator<CommandOption> iterator) {
        CommandOption option;
        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--site-max-percent-cov-difference":
                    checkValueValid(1, 0, option);
                    siteCleanCutoff = getValidFloat(option);
                    break;
                case "--percent-region-covered":
                    minPercentRegionCovered = getValidFloat(option);
                    break;
                case "--relative-difference":
                    isRelativeDifference = true;
                    break;
                case "--min-coverage-fraction":
                    checkValueValid(1, 0, option);
                    minCoverageFraction = getValidFloat(option);
                    break;
                default:
                    continue;
            }
            iterator.remove();
        }
    }

    public static boolean isMinCoverageFractionValid(float value) {
        if (minCoverageFraction == Data.NO_FILTER) {
            return true;
        }

        return value >= minCoverageFraction
                && value != Data.NA;
    }
}
