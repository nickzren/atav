
import function.genotype.base.SampleManager;
import function.variant.base.RegionManager;
import function.annotation.base.EffectManager;
import function.variant.base.VariantManager;
import function.annotation.base.TranscriptManager;
import function.annotation.base.GeneManager;
import utils.CommandManager;
import utils.DBManager;
import utils.ErrorManager;
import utils.LogManager;
import function.AnalysisBase;
import function.genotype.collapsing.CollapsingCompHet;
import function.genotype.collapsing.CollapsingSingleVariant;
import function.coverage.comparison.CoverageComparison;
import function.coverage.summary.CoverageSummary;
import function.coverage.summary.SiteCoverageSummary;
import function.genotype.parental.ParentalMosaic;
import function.genotype.pedmap.PedMapGenerator;
import function.genotype.sibling.ListSiblingComphet;
import function.genotype.statistics.FisherExactTest;
import function.genotype.statistics.LinearRegression;
import function.genotype.trio.ListTrio;
import function.annotation.varanno.ListVarAnno;
import function.annotation.varanno.VarAnnoCommand;
import function.coverage.base.CoverageCommand;
import function.coverage.comparison.SiteCoverageComparison;
import function.external.bis.BisCommand;
import function.external.bis.BisManager;
import function.external.bis.ListBis;
import function.external.denovo.DenovoDBCommand;
import function.external.denovo.ListDenovoDB;
import function.external.discovehr.DiscovEHRCommand;
import function.external.discovehr.ListDiscovEHR;
import function.external.evs.EvsCommand;
import function.genotype.vargeno.ListVarGeno;
import function.external.evs.ListEvs;
import function.external.exac.ExacCommand;
import function.external.exac.ListExac;
import function.external.gnomad.GnomADCommand;
import function.external.gnomad.GnomADManager;
import function.external.gnomad.ListGnomADExome;
import function.external.flanking.FlankingCommand;
import function.external.flanking.ListFlankingSeq;
import function.external.genomes.GenomesCommand;
import function.external.genomes.List1000Genomes;
import function.external.gerp.GerpCommand;
import function.external.gerp.ListGerp;
import function.external.gnomad.ListGnomADGenome;
import function.external.kaviar.KaviarCommand;
import function.external.kaviar.ListKaviar;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarManager;
import function.external.knownvar.ListKnownVar;
import function.external.mgi.ListMgi;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
import function.external.rvis.ListRvis;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.ListSubRvis;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisManager;
import function.external.trap.ListTrap;
import function.external.trap.TrapCommand;
import function.genotype.base.DPBinBlockManager;
import function.genotype.collapsing.CollapsingCommand;
import function.genotype.parental.ParentalCommand;
import function.genotype.pedmap.PedMapCommand;
import function.genotype.sibling.SiblingCommand;
import function.genotype.statistics.StatisticsCommand;
import function.genotype.trio.TrioCommand;
import function.genotype.var.ListVar;
import function.genotype.var.VarCommand;
import function.genotype.vargeno.VarGenoCommand;
import function.test.Test;
import function.test.TestCommand;
import utils.RunTimeManager;

/**
 *
 * @author nick
 */
public class Program {

    public static void main(String[] args) {
        try {
            RunTimeManager.start();

            // initialized user input options and system default input values
            init(args);

            // start one analysis function
            startAnalysis();

            RunTimeManager.stop();

            LogManager.run();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void init(String[] options) {
        try {
            CommandManager.initOptions(options);

            DBManager.init();

            EffectManager.init();

            SampleManager.init();

            RegionManager.init();

            GeneManager.init();

            TranscriptManager.init();

            VariantManager.init();

            DPBinBlockManager.init();

            KnownVarManager.init();

            RvisManager.init();

            SubRvisManager.init();

            BisManager.init();

            MgiManager.init();

            GnomADManager.init();

            // output external data version
            LogManager.logExternalDataVersion();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void startAnalysis() {
        try {
            if (VarGenoCommand.isListVarGeno) { // Genotype Analysis Functions
                runAnalysis(new ListVarGeno());
            } else if (VarCommand.isListVar) {
                runAnalysis(new ListVar());
            } else if (CollapsingCommand.isCollapsingSingleVariant) {
                runAnalysis(new CollapsingSingleVariant());
            } else if (CollapsingCommand.isCollapsingCompHet) {
                runAnalysis(new CollapsingCompHet());
            } else if (StatisticsCommand.isFisher) {
                runAnalysis(new FisherExactTest());
            } else if (StatisticsCommand.isLinear) {
                runAnalysis(new LinearRegression());
            } else if (SiblingCommand.isSiblingCompHet) {
                runAnalysis(new ListSiblingComphet());
            } else if (TrioCommand.isListTrio) {
                runAnalysis(new ListTrio());
            } else if (ParentalCommand.isParentalMosaic) {
                runAnalysis(new ParentalMosaic());
            } else if (PedMapCommand.isPedMap) {
                runAnalysis(new PedMapGenerator());
            } else if (VarAnnoCommand.isListVarAnno) { // Variant Annotation Functions
                runAnalysis(new ListVarAnno());
            } else if (CoverageCommand.isCoverageSummary) { // Coverage Analysis Functions
                runAnalysis(new CoverageSummary());
            } else if (CoverageCommand.isSiteCoverageSummary) {
                runAnalysis(new SiteCoverageSummary());
            } else if (CoverageCommand.isCoverageComparison) {
                runAnalysis(new CoverageComparison());
            } else if (CoverageCommand.isSiteCoverageComparison) {
                runAnalysis(new SiteCoverageComparison());
            } else if (EvsCommand.isListEvs) { // External Datasets Functions
                runAnalysis(new ListEvs());
            } else if (ExacCommand.isListExac) {
                runAnalysis(new ListExac());
            } else if (GnomADCommand.isListGnomADExome) {
                runAnalysis(new ListGnomADExome());
            } else if (GnomADCommand.isListGnomADGenome) {
                runAnalysis(new ListGnomADGenome());
            } else if (KnownVarCommand.isListKnownVar) {
                runAnalysis(new ListKnownVar());
            } else if (FlankingCommand.isListFlankingSeq) {
                runAnalysis(new ListFlankingSeq());
            } else if (KaviarCommand.isListKaviar) {
                runAnalysis(new ListKaviar());
            } else if (GerpCommand.isListGerp) {
                runAnalysis(new ListGerp());
            } else if (TrapCommand.isListTrap) {
                runAnalysis(new ListTrap());
            } else if (SubRvisCommand.isListSubRvis) {
                runAnalysis(new ListSubRvis());
            } else if (BisCommand.isListBis) {
                runAnalysis(new ListBis());
            } else if (RvisCommand.isListRvis) {
                runAnalysis(new ListRvis());
            } else if (GenomesCommand.isList1000Genomes) {
                runAnalysis(new List1000Genomes());
            } else if (MgiCommand.isListMgi) {
                runAnalysis(new ListMgi());
            } else if (DenovoDBCommand.isListDenovoDB) {
                runAnalysis(new ListDenovoDB());
            } else if (DiscovEHRCommand.isListDiscovEHR) {
                runAnalysis(new ListDiscovEHR());
            } else if (TestCommand.isTest) { // Test Functions
                runAnalysis(new Test());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void runAnalysis(AnalysisBase analysis) {
        LogManager.writeAndPrint(analysis.toString());

        analysis.run();
    }
}
