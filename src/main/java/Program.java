
import function.cohort.base.SampleManager;
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
import function.cohort.collapsing.CollapsingCompHet;
import function.cohort.collapsing.CollapsingSingleVariant;
import function.coverage.comparison.CoverageComparison;
import function.coverage.summary.CoverageSummary;
import function.coverage.summary.SiteCoverageSummary;
import function.cohort.parental.ParentalMosaic;
import function.cohort.pedmap.PedMapGenerator;
import function.cohort.sibling.ListSiblingComphet;
import function.cohort.statistics.FisherExactTest;
import function.cohort.statistics.LinearRegression;
import function.cohort.trio.ListTrio;
import function.annotation.varanno.ListVarAnno;
import function.annotation.varanno.VarAnnoCommand;
import function.cohort.af.AFCommand;
import function.cohort.af.ListAF;
import function.cohort.base.CarrierBlockManager;
import function.coverage.base.CoverageCommand;
import function.coverage.comparison.SiteCoverageComparison;
import function.external.ccr.CCRCommand;
import function.external.ccr.CCRManager;
import function.external.ccr.ListCCR;
import function.external.limbr.LIMBRCommand;
import function.external.limbr.LIMBRManager;
import function.external.limbr.ListLIMBR;
import function.external.denovo.DenovoDBCommand;
import function.external.denovo.ListDenovoDB;
import function.external.discovehr.DiscovEHRCommand;
import function.external.discovehr.ListDiscovEHR;
import function.external.evs.EvsCommand;
import function.cohort.vargeno.ListVarGeno;
import function.external.evs.ListEvs;
import function.external.exac.ExACCommand;
import function.external.exac.ExACManager;
import function.external.exac.ListExAC;
import function.external.gnomad.GnomADManager;
import function.external.gnomad.ListGnomADExome;
import function.external.gerp.GerpCommand;
import function.external.gerp.ListGerp;
import function.external.gnomad.ListGnomADGenome;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarManager;
import function.external.knownvar.ListKnownVar;
import function.external.mgi.ListMgi;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
import function.external.primateai.ListPrimateAI;
import function.external.primateai.PrimateAICommand;
import function.external.revel.ListRevel;
import function.external.revel.RevelCommand;
import function.external.rvis.ListRvis;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.ListSubRvis;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisManager;
import function.external.trap.ListTrap;
import function.external.trap.TrapCommand;
import function.cohort.base.DPBinBlockManager;
import function.cohort.collapsing.CollapsingCommand;
import function.cohort.collapsing.CollapsingLite;
import function.cohort.collapsing.CollapsingVCFLite;
import function.cohort.parent.ListParentCompHet;
import function.cohort.parent.ParentCommand;
import function.cohort.parental.ParentalCommand;
import function.cohort.pedmap.PedMapCommand;
import function.cohort.sibling.SiblingCommand;
import function.cohort.statistics.StatisticsCommand;
import function.cohort.trio.TrioCommand;
import function.cohort.var.ListVar;
import function.cohort.var.VarCommand;
import function.cohort.vargeno.ListVarGenoLite;
import function.cohort.vargeno.VarGenoCommand;
import function.cohort.vcf.ListVCF;
import function.cohort.vcf.ListVCFLite;
import function.cohort.vcf.VCFCommand;
import function.external.chm.CHMManager;
import function.external.dbnsfp.DBNSFPManager;
import function.external.defaultcontrolaf.DefaultControlAFManager;
import function.external.denovo.DenovoDBManager;
import function.external.discovehr.DiscovEHRManager;
import function.external.evs.EvsManager;
import function.external.genomeasia.GenomeAsiaCommand;
import function.external.genomeasia.GenomeAsiaManager;
import function.external.genomeasia.ListGenomeAsia;
import function.external.gerp.GerpManager;
import function.external.gevir.GeVIRManager;
import function.external.gme.GMECommand;
import function.external.gme.GMEManager;
import function.external.gme.ListGME;
import function.external.gnomad.GnomADExomeCommand;
import function.external.gnomad.GnomADGenomeCommand;
import function.external.igmaf.IGMAFManager;
import function.external.iranome.IranomeCommand;
import function.external.iranome.IranomeManager;
import function.external.iranome.ListIranome;
import function.external.mpc.ListMPC;
import function.external.mpc.MPCCommand;
import function.external.mpc.MPCManager;
import function.external.mtr.MTRManager;
import function.external.pext.ListPext;
import function.external.pext.PextCommand;
import function.external.pext.PextManager;
import function.external.primateai.PrimateAIManager;
import function.external.revel.RevelManager;
import function.external.synrvis.SynRvisManager;
import function.external.topmed.ListTopMed;
import function.external.topmed.TopMedCommand;
import function.external.topmed.TopMedManager;
import function.external.trap.TrapManager;
import function.test.Test;
import function.test.TestCommand;
import utils.EmailManager;
import utils.RunTimeManager;
import utils.ThirdPartyToolManager;

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
            EmailManager.init();

            CommandManager.initOptions(options);

            DBManager.init();
            
            ThirdPartyToolManager.init();

            EffectManager.init();

            SampleManager.init();

            RegionManager.init();

            GeneManager.init();

            TranscriptManager.init();

            VariantManager.init();
            
            CarrierBlockManager.init();
            
            DPBinBlockManager.init();

            EvsManager.init();
            
            ExACManager.init();

            KnownVarManager.init();

            RvisManager.init();

            SubRvisManager.init();
            
            GeVIRManager.init();
            
            SynRvisManager.init();

            LIMBRManager.init();

            MgiManager.init();
            
            DenovoDBManager.init();

            DiscovEHRManager.init();

            MTRManager.init();
            
            RevelManager.init();
            
            PrimateAIManager.init();
            
            GnomADManager.init();

            CCRManager.init();
            
            GerpManager.init();
            
            TrapManager.init();

            MPCManager.init();
            
            PextManager.init();
            
            CHMManager.init();

            GMEManager.init();
            
            TopMedManager.init();
            
            GenomeAsiaManager.init();
            
            IranomeManager.init();
            
            IGMAFManager.init();
            
            DefaultControlAFManager.init();
            
            DBNSFPManager.init();

            // output external data version
            LogManager.logExternalDataVersion();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void startAnalysis() {
        try {
            if (VarGenoCommand.isList) { // Genotype Analysis Functions
                runAnalysis(new ListVarGeno());
            } else if (VarGenoCommand.isListLite) {
                ListVarGenoLite listVarGenoLite = new ListVarGenoLite();
                listVarGenoLite.run();
            } else if (VarCommand.isList) {
                runAnalysis(new ListVar());
            } else if (VCFCommand.isList) {
                runAnalysis(new ListVCF());
            } else if (VCFCommand.isListLite) {
               ListVCFLite listVCFLite = new ListVCFLite();
               listVCFLite.run();
            } else if (AFCommand.isList) {
                runAnalysis(new ListAF());
            } else if (CollapsingCommand.isCollapsingSingleVariant) {
                runAnalysis(new CollapsingSingleVariant());
            } else if (CollapsingCommand.isCollapsingCompHet) {
                runAnalysis(new CollapsingCompHet());
            } else if (CollapsingCommand.isCollapsingLite) {
                CollapsingLite collapsingLite = new CollapsingLite();
                collapsingLite.run();
            } else if (CollapsingCommand.isCollapsingVCFLite) {
                CollapsingVCFLite collapsingVCFLite = new CollapsingVCFLite();
                collapsingVCFLite.run();
            } else if (StatisticsCommand.isFisher) {
                runAnalysis(new FisherExactTest());
            } else if (StatisticsCommand.isLinear) {
                runAnalysis(new LinearRegression());
            } else if (SiblingCommand.isSiblingCompHet) {
                runAnalysis(new ListSiblingComphet());
            } else if (TrioCommand.isList) {
                runAnalysis(new ListTrio());
            } else if (ParentCommand.isList) {
                runAnalysis(new ListParentCompHet());
            } else if (ParentalCommand.isParentalMosaic) {
                runAnalysis(new ParentalMosaic());
            } else if (PedMapCommand.isPedMap) {
                runAnalysis(new PedMapGenerator());
            } else if (VarAnnoCommand.isList) { // Variant Annotation Functions
                runAnalysis(new ListVarAnno());
            } else if (CoverageCommand.isCoverageSummary) { // Coverage Analysis Functions
                runAnalysis(new CoverageSummary());
            } else if (CoverageCommand.isSiteCoverageSummary) {
                runAnalysis(new SiteCoverageSummary());
            } else if (CoverageCommand.isCoverageComparison) {
                runAnalysis(new CoverageComparison());
            } else if (CoverageCommand.isSiteCoverageComparison) {
                runAnalysis(new SiteCoverageComparison());
            } else if (EvsCommand.isList) { // External Datasets Functions
                runAnalysis(new ListEvs());
            } else if (ExACCommand.getInstance().isList) {
                runAnalysis(new ListExAC());
            } else if (GnomADExomeCommand.getInstance().isList) {
                runAnalysis(new ListGnomADExome());
            } else if (GnomADGenomeCommand.getInstance().isList) {
                runAnalysis(new ListGnomADGenome());
            } else if (KnownVarCommand.isList) {
                runAnalysis(new ListKnownVar());
            } else if (GerpCommand.isList) {
                runAnalysis(new ListGerp());
            } else if (TrapCommand.isList) {
                runAnalysis(new ListTrap());
            } else if (SubRvisCommand.isList) {
                runAnalysis(new ListSubRvis());
            } else if (LIMBRCommand.isList) {
                runAnalysis(new ListLIMBR());
            } else if (RvisCommand.isList) {
                runAnalysis(new ListRvis());
            } else if (MgiCommand.isList) {
                runAnalysis(new ListMgi());
            } else if (DenovoDBCommand.isList) {
                runAnalysis(new ListDenovoDB());
            } else if (DiscovEHRCommand.isList) {
                runAnalysis(new ListDiscovEHR());
            } else if (RevelCommand.isList) {
                runAnalysis(new ListRevel());
            } else if (PrimateAICommand.isList) {
                runAnalysis(new ListPrimateAI());
            } else if (CCRCommand.isList) {
                runAnalysis(new ListCCR());
            } else if (PextCommand.isList) {
                runAnalysis(new ListPext());
            } else if (MPCCommand.isList) {
                runAnalysis(new ListMPC());
            } else if (GMECommand.getInstance().isList) {
                runAnalysis(new ListGME());
            } else if (TopMedCommand.getInstance().isList) {
                runAnalysis(new ListTopMed());
            } else if (GenomeAsiaCommand.getInstance().isList) {
                runAnalysis(new ListGenomeAsia());
            } else if (IranomeCommand.getInstance().isList) {
                runAnalysis(new ListIranome());
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
