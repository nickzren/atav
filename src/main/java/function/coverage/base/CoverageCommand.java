package function.coverage.base;

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
public class CoverageCommand {

    // coverage summary
    public static boolean isCoverageSummary = false;
    public static boolean isSiteCoverageSummary = false;
    public static double minPercentRegionCovered = 0; //so all is output by default
    public static double exonCleanCutoff = Data.NO_FILTER;
    public static double geneCleanCutoff = 1.0;
    public static double siteCleanCutoff = Data.NO_FILTER;

    // coverage comparison 
    public static boolean isCoverageComparison = false;
    public static boolean isSiteCoverageComparison = false;
    public static boolean isLinear = false;
    //public static double ExonMaxCovDiffPValue = 0.0;
    //public static double ExonMaxPercentVarExplained = 100.0;

    public static void initCoverageComparison(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--exon-max-percent-cov-difference":
                    checkValueValid(1, 0, option);
                    exonCleanCutoff = getValidDouble(option);
                    break;
                case "--gene-max-percent-cov-difference":
                    checkValueValid(1, 0, option);
                    geneCleanCutoff = getValidDouble(option);
                    break;
            /*else if (option.getName().equals("--exon-max-cov-diff-p-value")) {
            checkValueValid(1, 0, option);
            ExonMaxCovDiffPValue = getValidDouble(option);
            } else if (option.getName().equals("--exon-max-percent-var-explained")) {
            checkValueValid(100, 0, option);
            ExonMaxPercentVarExplained = getValidDouble(option);
            } */
                case "--quantitative":
                    isLinear = true;
                    StatisticsCommand.quantitativeFile = getValidPath(option);
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
                minPercentRegionCovered = getValidDouble(option);
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
                    siteCleanCutoff = getValidDouble(option);
                    break;
                case "--percent-region-covered":
                    minPercentRegionCovered = getValidDouble(option);
                    break;
                case "--gene-max-percent-cov-difference":
                    checkValueValid(1, 0, option);
                    geneCleanCutoff = getValidDouble(option);
                    break;
                default:
                    continue;
            }
            iterator.remove();
        }
    }

    public static String checkGeneCleanCutoff(double absDiff, double caseAvg, double ctrlAvg) {
        if (absDiff != Data.NA
                && absDiff > geneCleanCutoff) {
            if (caseAvg < ctrlAvg) {
                return "bias against discovery";
            } else {
                return "bias for discovery";
            }
        } else {
            return "none";
        }
    }
}
