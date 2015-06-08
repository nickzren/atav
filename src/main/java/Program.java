
import function.genotype.base.SampleManager;
import function.variant.base.RegionManager;
import function.annotation.base.FunctionManager;
import function.variant.base.VariantManager;
import function.annotation.base.IntolerantScoreManager;
import function.annotation.base.TranscriptManager;
import function.annotation.base.GeneManager;
import utils.CommandManager;
import utils.DBManager;
import utils.ErrorManager;
import utils.CommandValue;
import utils.LogManager;
import function.AnalysisBase;
import function.genotype.collapsing.CollapsingCompHet;
import function.genotype.collapsing.CollapsingSingleVariant;
import function.coverage.comparison.CoverageComparison;
import function.coverage.summary.CoverageSummary;
import function.coverage.summary.CoverageSummaryPipeline;
import function.coverage.summary.CoverageSummarySite;
import function.genotype.family.FamilyAnalysis;
import function.genotype.parental.ParentalMosaic;
import function.genotype.pedmap.PedMapGenerator;
import function.genotype.sibling.ListSiblingComphet;
import function.genotype.statistics.FisherExactTest;
import function.genotype.statistics.LinearRegression;
import function.genotype.trio.ListTrioCompHet;
import function.genotype.trio.ListTrioDenovo;
import function.annotation.genedx.ListGeneDx;
import function.annotation.varanno.ListNewVarId;
import function.annotation.varanno.ListVarAnno;
import function.genotype.vargeno.ListVarGeno;
import function.external.evs.JonEvsTool;
import function.external.evs.ListEvs;
import function.external.exac.ListExac;
import function.external.flanking.ListFlankingSeq;
import function.external.knownvar.ListKnownVar;
import function.genotype.base.CoverageBlockManager;

/**
 *
 * @author nick
 */
public class Program {

    private static long start, end;

    public static void main(String[] args) {
        try {
            start = System.currentTimeMillis();

            init(args);

            startAnalysis();

            SampleManager.recheckSampleList();

            end = System.currentTimeMillis();

            outputRuntime();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void init(String[] options) {
        try {
            CommandManager.initOptions(options);

            DBManager.init();

            FunctionManager.init();

            SampleManager.init();

            RegionManager.init();

            GeneManager.init();

            TranscriptManager.init();

            IntolerantScoreManager.init();

            VariantManager.init();

            CoverageBlockManager.init();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void startAnalysis() {
        try {
            if (CommandValue.isPedMap) {
                runAnalysis(new PedMapGenerator());
            } else if (CommandValue.isFisher) {
                runAnalysis(new FisherExactTest());
            } else if (CommandValue.isLinear) {
                runAnalysis(new LinearRegression());
            } else if (CommandValue.isCollapsingSingleVariant) {
                runAnalysis(new CollapsingSingleVariant());
            } else if (CommandValue.isCollapsingCompHet) {
                runAnalysis(new CollapsingCompHet());
            } else if (CommandValue.isListVarGeno) {
                runAnalysis(new ListVarGeno());
            } else if (CommandValue.isTrioDenovo) {
                runAnalysis(new ListTrioDenovo());
            } else if (CommandValue.isTrioCompHet) {
                runAnalysis(new ListTrioCompHet());
            } else if (CommandValue.isFamilyAnalysis) {
                runAnalysis(new FamilyAnalysis());
            } else if (CommandValue.isListVarAnno) {
                runAnalysis(new ListVarAnno());
            } else if (CommandValue.isListNewVarId) {
                runAnalysis(new ListNewVarId());
            } else if (CommandValue.isListGeneDx) {
                runAnalysis(new ListGeneDx());
            } else if (CommandValue.isListFlankingSeq) {
                runAnalysis(new ListFlankingSeq());
            } else if (CommandValue.isListKnownVar) {
                runAnalysis(new ListKnownVar());
            } else if (CommandValue.isListEvs) {
                runAnalysis(new ListEvs());
            } else if (CommandValue.isListExac) {
                runAnalysis(new ListExac());
            } else if (CommandValue.isJonEvsTool) {
                runAnalysis(new JonEvsTool());
            } else if (CommandValue.isCoverageComparison) {
                LogManager.writeAndPrint("It is running a coverage comparison function...");
                CoverageComparison coverageList = new CoverageComparison();
                coverageList.run();
            } else if (CommandValue.isCoverageSummary) {
                LogManager.writeAndPrint("It is running a coverage summary function...");
                CoverageSummary coverageList = new CoverageSummary();
                coverageList.run();
            } else if (CommandValue.isSiteCoverageSummary) {
                LogManager.writeAndPrint("It is running a site coverage summary function...");
                CoverageSummarySite coverageList = new CoverageSummarySite();
                coverageList.run();
            } else if (CommandValue.isCoverageSummaryPipeline) {
                LogManager.writeAndPrint("It is running a coverage summary for pipeline function...");
                CoverageSummaryPipeline coverageSummary = new CoverageSummaryPipeline();
                coverageSummary.run();
            } else if (CommandValue.isSiblingCompHet) {
                runAnalysis(new ListSiblingComphet());
            } else if (CommandValue.isParentalMosaic) {
                runAnalysis(new ParentalMosaic());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void runAnalysis(AnalysisBase analysis) {
        LogManager.writeAndPrint(analysis.toString());

        analysis.run();
    }

    private static void outputRuntime() {
        int seconds = (int) (Math.rint(end - start) / 1000);
        int minutes = seconds / 60;
        int hours = seconds / 3600;

        LogManager.writeAndPrint("\nRunning Time: "
                + seconds + " seconds "
                + "(aka " + minutes + " minutes or "
                + hours + " hours).\n");

        LogManager.close();
    }
}
