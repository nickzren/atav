package function.coverage.base;

import function.cohort.statistics.StatisticsCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class CoverageCommand {

    // coverage base 
    public static boolean isIncludeCoverageDetail = false;
    
    // coverage summary
    public static boolean isCoverageSummary = false;
    public static boolean isSiteCoverageSummary = false;
    public static float minPercentRegionCovered = 0; //so all is output by default
    public static float exonCleanCutoff = Data.NO_FILTER;
    public static float geneCleanCutoff = 1;
    public static float siteCleanCutoff = Data.NO_FILTER;

    // coverage comparison 
    public static boolean isCoverageComparison = false;
    public static boolean isSiteCoverageComparison = false;
    public static boolean isRelativeDifference = false;
    public static boolean isLinear = false;

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
                case "--include-coverage-detail":
                    isIncludeCoverageDetail = true;;
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
            switch (option.getName()) {
                case "--percent-region-covered":
                    minPercentRegionCovered = getValidFloat(option);
                    break;
                case "--include-coverage-detail":
                    isIncludeCoverageDetail = true;
                    break;
                default:
                    continue;
            }
            iterator.remove();
        }
    }

    public static void initSiteCoverageComparison(Iterator<CommandOption> iterator) {
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
                case "--include-coverage-detail":
                    isIncludeCoverageDetail = true;
                    break;
                default:
                    continue;
            }
            iterator.remove();
        }
    }
    
    public static void initSiteCoverageSummary(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--include-coverage-detail":
                    isIncludeCoverageDetail = true;
                    break;
                default:
                    continue;
            }
            iterator.remove();
        }
    }
}
