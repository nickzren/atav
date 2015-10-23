
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
import utils.LogManager;
import function.AnalysisBase;
import function.annotation.genedx.GeneDxCommand;
import function.genotype.collapsing.CollapsingCompHet;
import function.genotype.collapsing.CollapsingSingleVariant;
import function.coverage.comparison.CoverageComparison;
import function.coverage.summary.CoverageSummary;
import function.coverage.summary.CoverageSummaryPipeline;
import function.coverage.summary.SiteCoverageSummary;
import function.genotype.family.FamilyAnalysis;
import function.genotype.parental.ParentalMosaic;
import function.genotype.pedmap.PedMapGenerator;
import function.genotype.sibling.ListSiblingComphet;
import function.genotype.statistics.FisherExactTest;
import function.genotype.statistics.LinearRegression;
import function.genotype.trio.ListTrioCompHet;
import function.genotype.trio.ListTrioDenovo;
import function.annotation.genedx.ListGeneDx;
import function.annotation.varanno.ListVarAnno;
import function.annotation.varanno.VarAnnoCommand;
import function.coverage.base.CoverageCommand;
import function.coverage.comparison.SiteCoverageComparison;
import function.external.evs.EvsCommand;
import function.genotype.vargeno.ListVarGeno;
import function.external.evs.ListEvs;
import function.external.exac.ExacCommand;
import function.external.exac.ListExac;
import function.external.flanking.FlankingCommand;
import function.external.flanking.ListFlankingSeq;
import function.external.kaviar.KaviarCommand;
import function.external.kaviar.ListKaviar;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.ListKnownVar;
import function.genotype.base.CoverageBlockManager;
import function.genotype.collapsing.CollapsingCommand;
import function.genotype.family.FamilyCommand;
import function.genotype.parental.ParentalCommand;
import function.genotype.pedmap.PedMapCommand;
import function.genotype.sibling.SiblingCommand;
import function.genotype.statistics.StatisticsCommand;
import function.genotype.trio.TrioCommand;
import function.genotype.vargeno.VarGenoCommand;
import function.nondb.ppi.PPI;
import function.nondb.ppi.PPICommand;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author nick
 */
public class Program {

    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();

            init(args);

            startAnalysis();

            SampleManager.recheckSampleList();

            outputRuntime(start);

            LogManager.recordUserCommand();

            LogManager.close();
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
            if (VarGenoCommand.isListVarGeno) { // Genotype Analysis Functions
                runAnalysis(new ListVarGeno());
            } else if (CollapsingCommand.isCollapsingSingleVariant) {
                runAnalysis(new CollapsingSingleVariant());
            } else if (CollapsingCommand.isCollapsingCompHet) {
                runAnalysis(new CollapsingCompHet());
            } else if (StatisticsCommand.isFisher) {
                runAnalysis(new FisherExactTest());
            } else if (StatisticsCommand.isLinear) {
                runAnalysis(new LinearRegression());
            } else if (FamilyCommand.isFamilyAnalysis) {
                runAnalysis(new FamilyAnalysis());
            } else if (SiblingCommand.isSiblingCompHet) {
                runAnalysis(new ListSiblingComphet());
            } else if (TrioCommand.isTrioDenovo) {
                runAnalysis(new ListTrioDenovo());
            } else if (TrioCommand.isTrioCompHet) {
                runAnalysis(new ListTrioCompHet());
            } else if (ParentalCommand.isParentalMosaic) {
                runAnalysis(new ParentalMosaic());
            } else if (PedMapCommand.isPedMap) {
                runAnalysis(new PedMapGenerator());
            } else if (VarAnnoCommand.isListVarAnno) { // Variant Annotation Functions
                runAnalysis(new ListVarAnno());
            } else if (GeneDxCommand.isListGeneDx) {
                runAnalysis(new ListGeneDx());
            } else if (CoverageCommand.isCoverageSummary) { // Coverage Analysis Functions
                LogManager.writeAndPrint("It is running a coverage summary function...");
                CoverageSummary coverageList = new CoverageSummary();
                coverageList.run();
            } else if (CoverageCommand.isSiteCoverageSummary) {
                LogManager.writeAndPrint("It is running a site coverage summary function...");
                SiteCoverageSummary coverageList = new SiteCoverageSummary();
                coverageList.run();
            } else if (CoverageCommand.isCoverageComparison) {
                LogManager.writeAndPrint("It is running a coverage comparison function...");
                CoverageComparison coverageList = new CoverageComparison();
                coverageList.run();
            } else if (CoverageCommand.isSiteCoverageComparison) {
                LogManager.writeAndPrint("It is running a site coverage comparison function...");
                SiteCoverageComparison coverageList = new SiteCoverageComparison();
                coverageList.run();
            } else if (CoverageCommand.isCoverageSummaryPipeline) {
                LogManager.writeAndPrint("It is running a coverage summary for pipeline function...");
                CoverageSummaryPipeline coverageSummary = new CoverageSummaryPipeline();
                coverageSummary.run();
            } else if (EvsCommand.isListEvs) { // External Datasets Functions
                runAnalysis(new ListEvs());
            } else if (ExacCommand.isListExac) {
                runAnalysis(new ListExac());
            } else if (KnownVarCommand.isListKnownVar) {
                runAnalysis(new ListKnownVar());
            } else if (FlankingCommand.isListFlankingSeq) {
                runAnalysis(new ListFlankingSeq());
            } else if (KaviarCommand.isListKaviar) {
                runAnalysis(new ListKaviar());
            } else if (PPICommand.isPPI) { // Non Database Functions
                PPI ppi = new PPI();
                LogManager.writeAndPrint(ppi.toString());
                ppi.run();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void runAnalysis(AnalysisBase analysis) {
        LogManager.writeAndPrint(analysis.toString());

        analysis.run();
    }

    private static void outputRuntime(long start) {
        long total = System.currentTimeMillis() - start;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(total);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(total);
        long hours = TimeUnit.MILLISECONDS.toHours(total);

        LogManager.totalRunTime = seconds + " seconds "
                + "(aka " + minutes + " minutes or "
                + hours + " hours)";

        LogManager.writeAndPrint("\nTotal runtime: "
                + LogManager.totalRunTime + "\n");
    }
}
