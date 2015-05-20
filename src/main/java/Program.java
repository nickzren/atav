
import atav.analysis.base.AnalysisBase;
import atav.analysis.collapsing.CollapsingCompHet;
import atav.analysis.collapsing.CollapsingSingleVariant;
import atav.analysis.coverage.CoverageComparison;
import atav.analysis.coverage.CoverageSummary;
import atav.analysis.coverage.CoverageSummaryPipeline;
import atav.analysis.coverage.CoverageSummarySite;
import atav.analysis.family.FamilyAnalysis;
import atav.analysis.pedmap.PedMapGenerator;
import atav.analysis.sibling.ListSiblingComphet;
import atav.analysis.statistics.FisherExactTest;
import atav.analysis.statistics.LinearRegression;
import atav.analysis.trio.ListTrioCompHet;
import atav.analysis.trio.ListTrioDenovo;
import atav.analysis.varanno.ListGeneDx;
import atav.analysis.varanno.ListVarAnno;
import atav.analysis.vargeno.ListVarGeno;
import atav.annotools.JonEvsTool;
import atav.annotools.ListEvs;
import atav.annotools.ListExac;
import atav.annotools.ListFlankingSeq;
import atav.annotools.ListKnownVar;
import atav.manager.data.*;
import atav.manager.data.CoverageBlockManager;
import atav.manager.utils.*;

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
            }
//            else if (CommandValue.istest) {
//                runAnalysis(new ListCalledVariant());
//            }
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
