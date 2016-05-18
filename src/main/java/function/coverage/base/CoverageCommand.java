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
    public static boolean isCoverageSummaryPipeline = false;
    public static String coveredRegionFile = "";
    public static boolean isCaseControlSeparate = false;
    public static double minPercentRegionCovered = 0; //so all is output by default 
    public static boolean isByExon = false;
    public static double exonCleanCutoff = -1.0; //not used by default
    public static double geneCleanCutoff = 1.0;
    public static double siteCleanCutoff = -1.0; // not used by default

    // coverage comparison 
    public static boolean isCoverageComparison = false;
    public static boolean isSiteCoverageComparison = false;
    public static boolean isCoverageComparisonDoLinear = false;
    //public static double ExonMaxCovDiffPValue = 0.0;
    //public static double ExonMaxPercentVarExplained = 100.0;

    public static void initCoverageSummaryPipeline(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--covered-region")) { //this will be the only option allowed for pipeline
                coveredRegionFile = getValidPath(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    public static void initCoverageComparison(Iterator<CommandOption> iterator) {
        CommandOption option;
        isByExon = true;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--gene-boundaries")
                    || option.getName().equals("--gene-boundary")) {
                coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--exon-max-percent-cov-difference")) {
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
            if (option.getName().equals("--covered-region")) {
                coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--percent-region-covered")) {
                minPercentRegionCovered = getValidDouble(option);
            } else if (option.getName().equals("--by-exon")
                    || option.getName().equals("--include-exon-file")) {
                isByExon = true;
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    public static void initSiteCoverageSummary(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--covered-region")) {
                coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--gene-boundaries")
                    || option.getName().equals("--gene-boundary")) {
                coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--case-control")) {
                isCaseControlSeparate = true;
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    public static void initCoverageComparisonSite(Iterator<CommandOption> iterator) {
        isCaseControlSeparate = true; // always true for comparison
        CommandOption option;
        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--covered-region")) {
                coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--gene-boundaries")
                    || option.getName().equals("--gene-boundary")) {
                coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--site-max-percent-cov-difference")) {
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
