package function.coverage.base;

import function.genotype.statistics.StatisticsCommand;
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
    public static double exonCleanCutoff = -1.0; //not used by default
    public static double geneCleanCutoff = 1.0;
    public static double siteCleanCutoff = -1.0; // not used by default

    // coverage comparison 
    public static boolean isCoverageComparison = false;
    public static boolean isSiteCoverageComparison = false;
    public static boolean isCoverageComparisonDoLinear = false;
    //public static double ExonMaxCovDiffPValue = 0.0;
    //public static double ExonMaxPercentVarExplained = 100.0;

    public static void initCoverageComparison(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--exon-max-percent-cov-difference")) {
                checkValueValid(1, 0, option);
                exonCleanCutoff = getValidDouble(option);
            } else if (option.getName().equals("--gene-max-percent-cov-difference")) {
                checkValueValid(1, 0, option);
                geneCleanCutoff = getValidDouble(option);
            } else if (option.getName().equals("--quantitative")) {
                isCoverageComparisonDoLinear = true;
                StatisticsCommand.quantitativeFile = getValidPath(option);
            } /*else if (option.getName().equals("--exon-max-cov-diff-p-value")) {
             checkValueValid(1, 0, option);
             ExonMaxCovDiffPValue = getValidDouble(option);
             } else if (option.getName().equals("--exon-max-percent-var-explained")) {
             checkValueValid(100, 0, option);
             ExonMaxPercentVarExplained = getValidDouble(option);
             } */ else {
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
            if (option.getName().equals("--site-max-percent-cov-difference")) {
                checkValueValid(1, 0, option);
                siteCleanCutoff = getValidDouble(option);
            } else if (option.getName().equals("--percent-region-covered")) {
                minPercentRegionCovered = getValidDouble(option);
            } else if (option.getName().equals("--gene-max-percent-cov-difference")) {
                checkValueValid(1, 0, option);
                geneCleanCutoff = getValidDouble(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }
}
