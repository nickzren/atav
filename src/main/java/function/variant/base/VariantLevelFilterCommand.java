package function.variant.base;

import function.external.limbr.LIMBRCommand;
import function.external.denovo.DenovoDBCommand;
import function.external.discovehr.DiscovEHRCommand;
import function.external.evs.EvsCommand;
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
import function.external.primateai.PrimateAICommand;
import function.external.revel.RevelCommand;
import function.external.rvis.RvisCommand;
import function.external.subrvis.SubRvisCommand;
import function.external.trap.TrapCommand;
import global.Data;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.getValidInteger;
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
    public static boolean isExcludeMultiallelicVariant = false;
    public static boolean isExcludeMultiallelicVariant2 = false;
    public static boolean isExcludeSnv = false;
    public static boolean isExcludeIndel = false;

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
                    includeVariantId = option.getValue();
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
                case "--evs-maf":
                    checkValueValid(0.5, 0, option);
                    EvsCommand.evsMaf = getValidDouble(option);
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
                case "--exac-af":
                    checkValueValid(1, 0, option);
                    ExacCommand.exacAF = getValidFloat(option);
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
                    GnomADCommand.gnomADExomePopSet = getSet(option);
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--gnomad-genome-pop":
                    checkValuesValid(GnomADManager.GNOMAD_GENOME_POP, option);
                    GnomADCommand.gnomADGenomePopSet = getSet(option);
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--gnomad-exome-af":
                    checkValueValid(1, 0, option);
                    GnomADCommand.gnomADExomeAF = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--gnomad-genome-af":
                    checkValueValid(1, 0, option);
                    GnomADCommand.gnomADGenomeAF = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--gnomad-exome-rf-tp-probability-snv":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADExomeRfTpProbabilitySnv = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--gnomad-exome-rf-tp-probability-indel":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADExomeRfTpProbabilityIndel = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;    
                case "--gnomad-genome-rf-tp-probability-snv":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADGenomeRfTpProbabilitySnv = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--gnomad-genome-rf-tp-probability-indel":
                    checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                    GnomADCommand.gnomADGenomeRfTpProbabilityIndel = getValidFloat(option);
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--known-var-only":
                    KnownVarCommand.isKnownVarOnly = true;
                    KnownVarCommand.isIncludeKnownVar = true;
                    break;
                case "--known-var-pathogenic-only":
                    KnownVarCommand.isKnownVarPathogenicOnly = true;
                    KnownVarCommand.isIncludeKnownVar = true;
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
                case "--min-trap-score-non-coding":
                    checkValueValid(Data.NO_FILTER, 0, option);
                    TrapCommand.minTrapScoreNonCoding = getValidFloat(option);
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
                case "--max-1000-genomes-af":
                    checkValueValid(1, 0, option);
                    GenomesCommand.maxGenomesAF = getValidFloat(option);
                    GenomesCommand.isInclude1000Genomes = true;
                    break;
                case "--sub-rvis-domain-score-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.subRVISDomainScorePercentile = getValidFloat(option);
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--mtr-domain-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.mtrDomainPercentile = getValidFloat(option);
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--sub-rvis-exon-score-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.subRVISExonScorePercentile = getValidFloat(option);
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--mtr-exon-percentile":
                    checkValueValid(100, 0, option);
                    SubRvisCommand.mtrExonPercentile = getValidFloat(option);
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--limbr-domain-percentile":
                    checkValueValid(100, 0, option);
                    LIMBRCommand.limbrDomainPercentile = getValidFloat(option);
                    LIMBRCommand.isIncludeLIMBR = true;
                    break;
                case "--limbr-exon-percentile":
                    checkValueValid(100, 0, option);
                    LIMBRCommand.limbrExonPercentile = getValidFloat(option);
                    LIMBRCommand.isIncludeLIMBR = true;
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
                    MTRCommand.mtrCentile = getValidFloat(option);
                    MTRCommand.isIncludeMTR = true;
                    break;
                case "--min-revel-score":
                    checkValueValid(1, 0, option);
                    RevelCommand.minRevel = getValidFloat(option);
                    RevelCommand.isIncludeRevel = true;
                    break;
                case "--min-primate-dl-score":
                    checkValueValid(1, 0, option);
                    PrimateAICommand.minPrimateDLScore = getValidFloat(option);
                    PrimateAICommand.isIncludePrimateAI = true;
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
//                case "--include-kaviar":
//                    KaviarCommand.isIncludeKaviar = true;
//                    break;
                case "--include-known-var":
                    KnownVarCommand.isIncludeKnownVar = true;
                    break;
                case "--include-rvis":
                    RvisCommand.isIncludeRvis = true;
                    break;
                case "--include-sub-rvis":
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--include-limbr":
                    LIMBRCommand.isIncludeLIMBR = true;
                    break;
//                case "--include-1000-genomes":
//                    GenomesCommand.isInclude1000Genomes = true;
//                    break;
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
                case "--include-revel":
                    RevelCommand.isIncludeRevel = true;
                    break;
                case "--include-primate-ai":
                    PrimateAICommand.isIncludePrimateAI = true;
                    break;    
                default:
                    continue;
            }

            iterator.remove();
        }
    }
    
    private static Set<String> getSet(CommandOption option) {
        String[] values = option.getValue().split(",");

        return new HashSet<>(Arrays.asList(values));
    }
}
