package function.variant.base;

import function.external.bis.BisCommand;
import function.external.denovo.DenovoDBCommand;
import function.external.discovehr.DiscovEHRCommand;
import function.external.evs.EvsCommand;
import function.external.evs.EvsManager;
import function.external.exac.ExacCommand;
import function.external.exac.ExacManager;
import function.external.gnomad.GnomADCommand;
import function.external.gnomad.GnomADManager;
import function.external.genomes.GenomesCommand;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpCommand;
import function.external.kaviar.KaviarCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.mgi.MgiCommand;
import function.external.mtr.MTRCommand;
import function.external.rvis.RvisCommand;
import function.external.subrvis.SubRvisCommand;
import function.external.trap.TrapCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
import utils.CommonCommand;
import static utils.CommandManager.checkValueValid;

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
    public static boolean isExcludeSnv = false;
    public static boolean isExcludeIndel = false;

    public static double minCscore = Data.NO_FILTER;

    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--region":
                    CommonCommand.regionInput = option.getValue();
                    break;
                case "--variant":
                    includeVariantId = getValidPath(option);
                    break;
                case "--rs-number":
                    includeRsNumber = getValidPath(option);
                    break;
                case "--exclude-variant":
                    excludeVariantId = getValidPath(option);
                    break;
                case "--exclude-artifacts":
                    isExcludeArtifacts = true;
                    break;
                case "--exclude-snv":
                    isExcludeSnv = true;
                    break;
                case "--exclude-indel":
                    isExcludeIndel = true;
                    break;
                case "--evs-pop":
                case "--evs-maf-pop":
                    checkValuesValid(EvsManager.EVS_POP, option);
                    EvsCommand.evsPop = option.getValue();
                    EvsCommand.isIncludeEvs = true;
                    break;
                case "--evs-maf":
                    checkValueValid(0.5, 0, option);
                    EvsCommand.evsMaf = getValidDouble(option);
                    EvsCommand.isIncludeEvs = true;
                    break;
                case "--min-evs-all-average-coverage":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    EvsCommand.evsAllAverageCoverage = getValidInteger(option);
                    EvsCommand.isIncludeEvs = true;
                    break;
                case "--exclude-evs-qc-failed":
                    EvsCommand.isExcludeEvsQcFailed = true;
                    EvsCommand.isIncludeEvs = true;
                    break;
                case "--exac-pop":
                    checkValuesValid(ExacManager.EXAC_POP, option);
                    ExacCommand.exacPop = option.getValue();
                    ExacCommand.isIncludeExac = true;
                    break;
                case "--exac-maf":
                    checkValueValid(0.5, 0, option);
                    ExacCommand.exacMaf = getValidFloat(option);
                    ExacCommand.isIncludeExac = true;
                    break;
                case "--min-exac-vqslod-snv":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    ExacCommand.exacVqslodSnv = getValidFloat(option);
                    ExacCommand.isIncludeExac = true;
                    break;
                case "--min-exac-vqslod-indel":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    ExacCommand.exacVqslodIndel = getValidFloat(option);
                    ExacCommand.isIncludeExac = true;
                    break;
                case "--gnomad-exome-pop":
                    checkValuesValid(GnomADManager.GNOMAD_EXOME_POP, option);
                    GnomADCommand.gnomADExomePop = option.getValue();
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--gnomad-genome-pop":
                    checkValuesValid(GnomADManager.GNOMAD_GENOME_POP, option);
                    GnomADCommand.gnomADGenomePop = option.getValue();
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--gnomad-exome-maf":
                    checkValueValid(0.5, 0, option);
                    GnomADCommand.gnomADExomeMaf = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--gnomad-genome-maf":
                    checkValueValid(0.5, 0, option);
                    GnomADCommand.gnomADGenomeMaf = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--gnomad-exome-as-rf-snv":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADExomeAsRfSnv = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--gnomad-genome-as-rf-snv":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADGenomeAsRfSnv = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--gnomad-exome-as-rf-indel":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADExomeAsRfIndel = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--gnomad-exome-ab-median":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADExomeABMedian = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--gnomad-genome-as-rf-indel":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADGenomeAsRfIndel = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--gnomad-genome-ab-median":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADGenomeABMedian = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--known-var-only":
                    KnownVarCommand.isKnownVarOnly = true;
                    KnownVarCommand.isIncludeKnownVar = true;
                    break;
                case "--min-c-score":
                case "--min-cadd-score":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    minCscore = getValidDouble(option);
                    break;
                case "--min-gerp-score":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    GerpCommand.minGerpScore = getValidFloat(option);
                    GerpCommand.isIncludeGerp = true;
                    break;
                case "--min-trap-score":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    TrapCommand.minTrapScore = getValidFloat(option);
                    TrapCommand.isIncludeTrap = true;
                    break;
                case "--max-kaviar-maf":
                    checkValueValid(1, 0, option);
                    KaviarCommand.maxKaviarMaf = getValidFloat(option);
                    KaviarCommand.isIncludeKaviar = true;
                    break;
                case "--max-kaviar-allele-count":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    KaviarCommand.maxKaviarAlleleCount = getValidInteger(option);
                    KaviarCommand.isIncludeKaviar = true;
                    break;
                case "--1000-genomes-pop":
                    checkValuesValid(GenomesManager.GENOMES_POP, option);
                    GenomesCommand.genomesPop = option.getValue();
                    GenomesCommand.isInclude1000Genomes = true;
                    break;
                case "--max-1000-genomes-maf":
                    checkValueValid(0.5, 0, option);
                    GenomesCommand.maxGenomesMaf = getValidFloat(option);
                    GenomesCommand.isInclude1000Genomes = true;
                    break;
                case "--sub-rvis-domain-score-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.subRVISDomainScorePercentile = getValidFloat(option);
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--sub-rvis-domain-oeratio-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.subRVISDomainOEratioPercentile = getValidFloat(option);
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--sub-rvis-exon-score-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.subRVISExonScorePercentile = getValidFloat(option);
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--sub-rvis-exon-oeratio-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.subRVISExonOEratioPercentile = getValidFloat(option);
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--bis-domain-score-percentile-0.005":
                    checkValueValid(100, 0, option);
                    BisCommand.bisDomainScorePercentile0005 = getValidFloat(option);
                    BisCommand.isIncludeBis = true;
                    break;
                case "--bis-domain-score-percentile-0.001":
                    checkValueValid(100, 0, option);
                    BisCommand.bisDomainScorePercentile0001 = getValidFloat(option);
                    BisCommand.isIncludeBis = true;
                    break;
                case "--bis-domain-score-percentile-0.0005":
                    checkValueValid(100, 0, option);
                    BisCommand.bisDomainScorePercentile00005 = getValidFloat(option);
                    BisCommand.isIncludeBis = true;
                    break;
                case "--bis-domain-score-percentile-0.0001":
                    checkValueValid(100, 0, option);
                    BisCommand.bisDomainScorePercentile00001 = getValidFloat(option);
                    BisCommand.isIncludeBis = true;
                    break;
                case "--bis-exon-score-percentile-0.005":
                    checkValueValid(100, 0, option);
                    BisCommand.bisExonScorePercentile0005 = getValidFloat(option);
                    BisCommand.isIncludeBis = true;
                    break;
                case "--bis-exon-score-percentile-0.001":
                    checkValueValid(100, 0, option);
                    BisCommand.bisExonScorePercentile0001 = getValidFloat(option);
                    BisCommand.isIncludeBis = true;
                    break;
                case "--bis-exon-score-percentile-0.0005":
                    checkValueValid(100, 0, option);
                    BisCommand.bisExonScorePercentile00005 = getValidFloat(option);
                    BisCommand.isIncludeBis = true;
                    break;
                case "--bis-exon-score-percentile-0.0001":
                    checkValueValid(100, 0, option);
                    BisCommand.bisExonScorePercentile00001 = getValidFloat(option);
                    BisCommand.isIncludeBis = true;
                    break;
                case "--discovehr-af":
                    checkValueValid(1, 0, option);
                    DiscovEHRCommand.discovEHRAF = getValidFloat(option);
                    DiscovEHRCommand.isIncludeDiscovEHR = true;
                    break;
                case "--mtr":
                    checkValueValid(2, 0, option);
                    MTRCommand.mtr = getValidFloat(option);
                    MTRCommand.isIncludeMTR = true;
                    break;
                case "--mtr-fdr":
                    checkValueValid(1, 0, option);
                    MTRCommand.fdr = getValidFloat(option);
                    MTRCommand.isIncludeMTR = true;
                    break;
                case "--mtr-centile":
                    checkValueValid(100, 0, option);
                    MTRCommand.mtrCentile = getValidInteger(option);
                    MTRCommand.isIncludeMTR = true;
                    break;
                case "--include-evs":
                    EvsCommand.isIncludeEvs = true;
                    break;
                case "--include-exac":
                    ExacCommand.isIncludeExac = true;
                    break;
                case "--include-exac-gene-variant-count":
                    ExacCommand.isIncludeExacGeneVariantCount = true;
                    break;
                case "--include-gnomad-exome":
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--include-gnomad-genome":
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--include-gerp":
                    GerpCommand.isIncludeGerp = true;
                    break;
                case "--include-trap":
                    TrapCommand.isIncludeTrap = true;
                    break;
                case "--include-kaviar":
                    KaviarCommand.isIncludeKaviar = true;
                    break;
                case "--include-known-var":
                    KnownVarCommand.isIncludeKnownVar = true;
                    break;
                case "--include-rvis":
                    RvisCommand.isIncludeRvis = true;
                    break;
                case "--include-sub-rvis":
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--include-bis":
                    BisCommand.isIncludeBis = true;
                    break;
                case "--include-1000-genomes":
                    GenomesCommand.isInclude1000Genomes = true;
                    break;
                case "--include-mgi":
                    MgiCommand.isIncludeMgi = true;
                    break;
                case "--include-denovo-db":
                    DenovoDBCommand.isIncludeDenovoDB = true;
                    break;
                case "--include-discovehr":
                    DiscovEHRCommand.isIncludeDiscovEHR = true;
                    break;
                case "--include-mtr":
                    MTRCommand.isIncludeMTR = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }

    public static boolean isCscoreValid(float value) {
        if (value == Data.NA
                || minCscore == Data.NO_FILTER) {
            return true;
        }

        return value >= minCscore;
    }
}
