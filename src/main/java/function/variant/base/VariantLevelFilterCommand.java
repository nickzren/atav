package function.variant.base;

import function.external.ccr.CCRCommand;
import function.external.chm.CHMCommand;
import function.external.limbr.LIMBRCommand;
import function.external.denovo.DenovoDBCommand;
import function.external.discovehr.DiscovEHRCommand;
import function.external.evs.EvsCommand;
import function.external.exac.ExACCommand;
import function.external.exac.ExACManager;
import function.external.genomeasia.GenomeAsiaCommand;
import function.external.gnomad.GnomADCommand;
import function.external.gnomad.GnomADManager;
import function.external.gerp.GerpCommand;
import function.external.gevir.GeVIRCommand;
import function.external.gme.GMECommand;
import function.external.igmaf.IGMAFCommand;
import function.external.iranome.IranomeCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.mgi.MgiCommand;
import function.external.mpc.MPCCommand;
import function.external.mtr.MTRCommand;
import function.external.pext.PextCommand;
import function.external.primateai.PrimateAICommand;
import function.external.revel.RevelCommand;
import function.external.rvis.RvisCommand;
import function.external.subrvis.SubRvisCommand;
import function.external.synrvis.SynRvisCommand;
import function.external.topmed.TopMedCommand;
import function.external.trap.TrapCommand;
import global.Data;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidFloat;
import utils.CommandOption;
import utils.CommonCommand;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.outputInvalidOptionValue;

/**
 *
 * @author nick
 */
public class VariantLevelFilterCommand {

    // Variant Level Filter Options
    public static String includeVariantId = "";
    public static String includeRsNumber = "";
    public static String excludeVariantId = "";
    public static boolean isExcludeArtifacts = false;
    public static boolean isExcludeMultiallelicVariant = false;
    public static boolean isExcludeMultiallelicVariant2 = false;
    public static boolean isExcludeSnv = false;
    public static boolean isExcludeIndel = false;
    public static boolean disableCheckOnSexChr = false;
    public static boolean isIncludeLOFTEE = false;
    public static boolean isExcludeFalseLOFTEE = false;

    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--region":
                    if (RegionManager.isRegionInputValid(option.getValue())) {
                        CommonCommand.regionInput = option.getValue();
                    } else {
                        outputInvalidOptionValue(option);
                    }
                    break;
                case "--variant":
                    if (VariantManager.isVariantIdInputValid(option.getValue())) {
                        includeVariantId = option.getValue();
                    } else {
                        outputInvalidOptionValue(option);
                    }
                    break;
                case "--rs-number":
                    includeRsNumber = option.getValue();
                    break;
                case "--exclude-variant":
                    excludeVariantId = option.getValue();
                    break;
                case "--exclude-artifacts":
                    isExcludeArtifacts = true;
                    break;
                case "--exclude-multiallelic-variant":
                    isExcludeMultiallelicVariant = true;
                    break;
                case "--exclude-multiallelic-variant-2":
                    isExcludeMultiallelicVariant2 = true;
                    break;
                case "--exclude-snv":
                    isExcludeSnv = true;
                    break;
                case "--exclude-indel":
                    isExcludeIndel = true;
                    break;
                case "--disable-check-on-sex-chr":
                    disableCheckOnSexChr = true;
                    break;
                case "--evs-maf":
                    checkValueValid(0.5, 0, option);
                    EvsCommand.evsMaf = getValidDouble(option);
                    EvsCommand.isInclude = true;
                    break;
                case "--exclude-evs-qc-failed":
                    EvsCommand.isExcludeEvsQcFailed = true;
                    EvsCommand.isInclude = true;
                    break;
                case "--exac-pop":
                    checkValuesValid(ExACManager.POP, option);
                    ExACCommand.pop = option.getValue();
                    ExACCommand.isInclude = true;
                    break;
                case "--exac-af":
                case "--max-exac-af":
                    checkValueValid(1, 0, option);
                    ExACCommand.maxAF = getValidFloat(option);
                    ExACCommand.isInclude = true;
                    break;
                case "--exac-maf":
                    checkValueValid(1, 0, option);
                    ExACCommand.maf = getValidFloat(option);
                    ExACCommand.isInclude = true;
                    break;    
                case "--min-exac-vqslod-snv":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    ExACCommand.vqslodSnv = getValidFloat(option);
                    ExACCommand.isInclude = true;
                    break;
                case "--min-exac-vqslod-indel":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    ExACCommand.vqslodIndel = getValidFloat(option);
                    ExACCommand.isInclude = true;
                    break;
                case "--gnomad-exome-pop":
                    checkValuesValid(GnomADManager.EXOME_POP, option);
                    GnomADCommand.exomePopSet = getSet(option);
                    GnomADCommand.isIncludeExome = true;
                    break;
                case "--gnomad-genome-pop":
                    checkValuesValid(GnomADManager.GENOME_POP, option);
                    GnomADCommand.genomePopSet = getSet(option);
                    GnomADCommand.isIncludeGenome = true;
                    break;
                case "--gnomad-exome-af":
                case "--max-gnomad-exome-af":
                    checkValueValid(1, 0, option);
                    GnomADCommand.maxExomeAF = getValidFloat(option);
                    GnomADCommand.isIncludeExome = true;
                    break;
                case "--gnomad-exome-maf":
                    checkValueValid(1, 0, option);
                    GnomADCommand.exomeMAF = getValidFloat(option);
                    GnomADCommand.isIncludeExome = true;
                    break;    
                case "--gnomad-genome-af":
                case "--max-gnomad-genome-af":
                    checkValueValid(1, 0, option);
                    GnomADCommand.maxGenomeAF = getValidFloat(option);
                    GnomADCommand.isIncludeGenome = true;
                    break;
                case "--gnomad-genome-maf":
                    checkValueValid(1, 0, option);
                    GnomADCommand.genomeMAF = getValidFloat(option);
                    GnomADCommand.isIncludeGenome = true;
                    break;      
                case "--gnomad-exome-rf-tp-probability-snv":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.exomeRfTpProbabilitySnv = getValidFloat(option);
                    GnomADCommand.isIncludeExome = true;
                    break;
                case "--gnomad-exome-rf-tp-probability-indel":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.exomeRfTpProbabilityIndel = getValidFloat(option);
                    GnomADCommand.isIncludeExome = true;
                    break;
                case "--gnomad-genome-rf-tp-probability-snv":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.genomeRfTpProbabilitySnv = getValidFloat(option);
                    GnomADCommand.isIncludeGenome = true;
                    break;
                case "--gnomad-genome-rf-tp-probability-indel":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.genomeRfTpProbabilityIndel = getValidFloat(option);
                    GnomADCommand.isIncludeGenome = true;
                    break;
                case "--known-var-only":
                    KnownVarCommand.isKnownVarOnly = true;
                    KnownVarCommand.isInclude = true;
                    break;
                case "--known-var-pathogenic-only":
                    KnownVarCommand.isKnownVarPathogenicOnly = true;
                    KnownVarCommand.isInclude = true;
                    break;
                case "--min-gerp-score":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    GerpCommand.minGerpScore = getValidFloat(option);
                    GerpCommand.isInclude = true;
                    break;
                case "--min-trap-score":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    TrapCommand.minTrapScore = getValidFloat(option);
                    TrapCommand.isInclude = true;
                    break;
                case "--sub-rvis-domain-score-percentile":
                case "--max-sub-rvis-domain-score-percentile":
                case "--max-sub-rvis-domain-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.maxSubRVISDomainPercentile = getValidFloat(option);
                    SubRvisCommand.isInclude = true;
                    break;
                case "--mtr-domain-percentile":
                case "--max-mtr-domain-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.maxMtrDomainPercentile = getValidFloat(option);
                    SubRvisCommand.isInclude = true;
                    break;
                case "--sub-rvis-exon-score-percentile":
                case "--max-sub-rvis-exon-score-percentile":
                case "--max-sub-rvis-exon-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.maxSubRVISExonPercentile = getValidFloat(option);
                    SubRvisCommand.isInclude = true;
                    break;
                case "--mtr-exon-percentile":
                case "--max-mtr-exon-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.maxMtrExonPercentile = getValidFloat(option);
                    SubRvisCommand.isInclude = true;
                    break;
                case "--limbr-domain-percentile":
                case "--max-limbr-domain-percentile":
                    checkValueValid(100, 0, option);
                    LIMBRCommand.maxLimbrDomainPercentile = getValidFloat(option);
                    LIMBRCommand.isInclude = true;
                    break;
                case "--limbr-exon-percentile":
                case "--max-limbr-exon-percentile":
                    checkValueValid(100, 0, option);
                    LIMBRCommand.maxLimbrExonPercentile = getValidFloat(option);
                    LIMBRCommand.isInclude = true;
                    break;
                case "--min-ccr-percentile":
                    checkValueValid(100, 0, option);
                    CCRCommand.minCCRPercentile = getValidFloat(option);
                    CCRCommand.isInclude = true;
                    break;
                case "--discovehr-af":
                    checkValueValid(1, 0, option);
                    DiscovEHRCommand.discovEHRAF = getValidFloat(option);
                    DiscovEHRCommand.isInclude = true;
                    break;
                case "--mtr":
                case "--max-mtr":
                    checkValueValid(2, 0, option);
                    MTRCommand.maxMTR = getValidFloat(option);
                    MTRCommand.isInclude = true;
                    break;
                case "--mtr-fdr":
                case "--max-mtr-fdr":
                    checkValueValid(1, 0, option);
                    MTRCommand.maxMTRFDR = getValidFloat(option);
                    MTRCommand.isInclude = true;
                    break;
                case "--mtr-centile":
                case "--max-mtr-centile":
                    checkValueValid(100, 0, option);
                    MTRCommand.maxMTRCentile = getValidFloat(option);
                    MTRCommand.isInclude = true;
                    break;
                case "--min-revel-score":
                    checkValueValid(1, 0, option);
                    RevelCommand.minRevel = getValidFloat(option);
                    RevelCommand.isInclude = true;
                    break;
                case "--min-primate-ai":
                    checkValueValid(1, 0, option);
                    PrimateAICommand.minPrimateAI = getValidFloat(option);
                    PrimateAICommand.isInclude = true;
                    break;
                case "--min-mpc":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    MPCCommand.minMPC = getValidFloat(option);
                    MPCCommand.isInclude = true;
                    break;
                case "--min-pext-ratio":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    PextCommand.minPextRatio = getValidFloat(option);
                    PextCommand.isInclude = true;
                    break;
                case "--max-gme-af":
                    checkValueValid(1, 0, option);
                    GMECommand.maxAF = getValidFloat(option);
                    GMECommand.isInclude = true;
                    break;
                case "--gme-maf":
                    checkValueValid(1, 0, option);
                    GMECommand.maf = getValidFloat(option);
                    GMECommand.isInclude = true;
                    break;    
                case "--max-top-med-af":
                    checkValueValid(1, 0, option);
                    TopMedCommand.maxAF = getValidFloat(option);
                    TopMedCommand.isInclude = true;
                    break;
                case "--top-med-maf":
                    checkValueValid(1, 0, option);
                    TopMedCommand.maf = getValidFloat(option);
                    TopMedCommand.isInclude = true;
                    break;    
                case "--max-genome-asia-af":
                    checkValueValid(1, 0, option);
                    GenomeAsiaCommand.maxAF = getValidFloat(option);
                    GenomeAsiaCommand.isInclude = true;
                    break;
                case "--genome-asia-maf":
                    checkValueValid(1, 0, option);
                    GenomeAsiaCommand.maf = getValidFloat(option);
                    GenomeAsiaCommand.isInclude = true;
                    break;
                case "--max-iranome-af":
                    checkValueValid(1, 0, option);
                    IranomeCommand.maxAF = getValidFloat(option);
                    IranomeCommand.isInclude = true;
                    break;
                case "--iranome-maf":
                    checkValueValid(1, 0, option);
                    IranomeCommand.maf = getValidFloat(option);
                    IranomeCommand.isInclude = true;
                    break;
                case "--max-igm-af":
                    checkValueValid(1, 0, option);
                    IGMAFCommand.maxAF = getValidFloat(option);
                    IGMAFCommand.isInclude = true;
                    break;
                case "--igm-maf":
                    checkValueValid(0.5, 0, option);
                    IGMAFCommand.maf = getValidFloat(option);
                    IGMAFCommand.isInclude = true;
                    break;   
                case "--include-evs":
                    EvsCommand.isInclude = true;
                    break;
                case "--include-exac":
                    ExACCommand.isInclude = true;
                    break;
                case "--include-exac-gene-variant-count":
                    ExACCommand.isIncludeCount = true;
                    break;
                case "--include-gnomad-exome":
                    GnomADCommand.isIncludeExome = true;
                    break;
                case "--include-gnomad-genome":
                    GnomADCommand.isIncludeGenome = true;
                    break;
                case "--include-gnomad-gene-metrics":
                    GnomADCommand.isIncludeGeneMetrics = true;
                    break;
                case "--include-gerp":
                    GerpCommand.isInclude = true;
                    break;
                case "--include-trap":
                    TrapCommand.isInclude = true;
                    break;
                case "--include-known-var":
                    KnownVarCommand.isInclude = true;
                    break;
                case "--include-omim":
                    KnownVarCommand.isIncludeOMIM = true;
                    break;
                case "--include-rvis":
                    RvisCommand.isInclude = true;
                    break;
                case "--include-sub-rvis":
                    SubRvisCommand.isInclude = true;
                    break;
                case "--include-gevir":
                    GeVIRCommand.isInclude = true;
                    break;
                case "--include-syn-rvis":
                    SynRvisCommand.isInclude = true;
                    break;
                case "--include-limbr":
                    LIMBRCommand.isInclude = true;
                    break;
                case "--include-ccr":
                    CCRCommand.isInclude = true;
                    break;
                case "--include-mgi":
                    MgiCommand.isInclude = true;
                    break;
                case "--include-denovo-db":
                    DenovoDBCommand.isInclude = true;
                    break;
                case "--include-discovehr":
                    DiscovEHRCommand.isInclude = true;
                    break;
                case "--include-mtr":
                    MTRCommand.isInclude = true;
                    break;
                case "--include-revel":
                    RevelCommand.isInclude = true;
                    break;
                case "--include-primate-ai":
                    PrimateAICommand.isInclude = true;
                    break;
                case "--include-loftee":
                    isIncludeLOFTEE = true;
                    break;
                case "--exclude-false-loftee":
                    isExcludeFalseLOFTEE = true;
                    isIncludeLOFTEE = true;
                    break;
                case "--include-mpc":
                    MPCCommand.isInclude = true;
                    break;
                case "--include-pext":
                    PextCommand.isInclude = true;
                    break;
                case "--flag-repeat-region":
                    CHMCommand.isFlag = true;
                    break;
                case "--exclude-repeat-region":
                    CHMCommand.isExclude = true;
                    break;
                case "--include-gme":
                    GMECommand.isInclude = true;
                    break;
                case "--include-top-med":
                    TopMedCommand.isInclude = true;
                    break;
                case "--include-genome-asia":
                    GenomeAsiaCommand.isInclude = true;
                    break;
                case "--include-iranome":
                    IranomeCommand.isInclude = true;
                    break;
                case "--include-igm-af":
                    IGMAFCommand.isInclude = true;
                    break;    
                default:
                    continue;
            }

            iterator.remove();
        }
    }

    // only true or null valid when applied --exclude-false-loftee
    public static boolean isLOFTEEValid(Boolean value) {
        if (!isExcludeFalseLOFTEE) {
            return true;
        }

        return value == null
                || value == true;
    }

    private static Set<String> getSet(CommandOption option) {
        String[] values = option.getValue().split(",");

        return new HashSet<>(Arrays.asList(values));
    }
}
