package utils;

import global.Data;

/**
 *
 * @author nick
 */
public class CommandValue {

    // common values
    public static boolean isDebug = false;

    public static boolean isNonDBAnalysis = false;
    public static boolean isNonSampleAnalysis = false;

    // public static boolean isDebug = true;
    public static String outputPath = "";
    public static String realOutputPath = "";
    public static String outputDirName = "";

    public static String regionInput = ""; // either a region or a region file path.

    // Genotype Analysis Functions
    //
    // list var geno
    public static boolean isListVarGeno = false;

    // collapsing
    public static boolean isCollapsingSingleVariant = false;
    public static boolean isCollapsingCompHet = false;
    public static boolean isRecessive = false;
    public static double varMissingRate = Double.MAX_VALUE;
    public static String coverageSummaryFile = "";
    public static double looMaf = Data.NO_FILTER;
    public static double looCombFreq = Data.NO_FILTER;
    public static boolean isCollapsingDoLinear = false;
    public static boolean isCollapsingDoLogistic = false;
    public static String geneBoundariesFile = "";

    // fisher & linear
    public static boolean isFisher = false;
    public static boolean isLinear = false;
    public static double threshold4Sort = Data.NO_FILTER;
    public static String[] models = {"allelic", "dominant", "recessive", "genotypic"};
    public static boolean isCaseOnly = false; // also listvargeno
    public static String covariateFile = "";
    public static String quantitativeFile = "";
    public static int minHomCaseRec = Data.NO_FILTER; // collasping, fisher, linear

    // family analysis
    public static boolean isFamilyAnalysis = false;
    public static String familyId = "";
    public static double popCtrlMaf = Data.NO_FILTER;

    // list sibling comp het
    public static boolean isSiblingCompHet = false;

    // trio denovo
    public static boolean isTrioDenovo = false;
    public static boolean isIncludeNoflag = false;

    // trio comp het
    public static boolean isTrioCompHet = false;
    public static double combFreq = Data.NO_FILTER;

    // parental mosaic
    public static boolean isParentalMosaic = false;
    public static double childQD = Data.NO_FILTER;
    public static double[] childHetPercentAltRead = null;
    public static double minChildBinomial = Data.NO_FILTER;
    public static double maxParentBinomial = Data.NO_FILTER;

    // ped map
    public static boolean isPedMap = false;
    public static boolean isVariantIdOnly = false;
    public static boolean isCombineMultiAlleles = false;
    public static boolean isEigenstrat = false;
    public static boolean useGroup = false;
    public static String GroupType = "control";
    public static String pedMapPath = "";
    public static double dmaf = 0.5;   // 0.01;

    // Variant Annotation Functions
    //
    //
    public static boolean isListVarAnno = false;
    public static boolean isListGeneDx = false;

    // Coverage Analysis Functions
    //
    // coverage summary
    public static boolean isCoverageSummary = false;
    public static boolean isSiteCoverageSummary = false;
    public static boolean isCoverageSummaryPipeline = false;
    public static String coveredRegionFile = "";
    public static double minPercentRegionCovered = 0; //so all is output by default 
    public static boolean isExcludeUTR = false;
    public static boolean isByExon = false;
    public static boolean isTerse = false;
    public static double exonCleanCutoff = -1; //not used by default
    public static double geneCleanCutoff = 1.0;

    // coverage comparison 
    public static boolean isCoverageComparison = false;
    public static boolean isCoverageComparisonDoLinear = false;
    public static double ExonMaxCovDiffPValue = 0.0;
    public static double ExonMaxPercentVarExplained = 100.0;

    // External Datasets Functions
    //
    // list evs
    public static boolean isListEvs = false;
    public static String variantInputFile = "";

    // list exac
    public static boolean isListExac = false;

    // list known var
    public static boolean isListKnownVar = false;
    public static int snvWidth = 2;
    public static int indelWidth = 9;

    // flanking seq
    public static boolean isListFlankingSeq = false;
    public static int width = 0;

    // jon evs tool
    public static boolean isJonEvsTool = false;
    public static String jonEvsInput = "";

    // Non Database Functions
    //
    // PPI
    public static boolean isPPI = false;
    public static String ppiExclude = "#N/A";
    public static String ppiFile = "";
    public static String ppiGenotypeFile = "";
    public static int ppiPermutaitons = 100;

    // Variant Level Filter Options
    public static String includeVariantId = "";
    public static String excludeVariantId = "";
    public static boolean isExcludeArtifacts = false;
    public static boolean isExcludeSnv = false;
    public static boolean isExcludeIndel = false;
    public static double evsMaf = Data.NO_FILTER;
    public static double evsMhgf4Recessive = Data.NO_FILTER;
    public static boolean isOldEvsUsed = false;
    public static boolean isExcludeEvsQcFailed = false;
    public static String exacPop = "global";
    public static float exacMaf = Data.NO_FILTER;
    public static float exacVqslodSnv = Data.NO_FILTER;
    public static float exacVqslodIndel = Data.NO_FILTER;
    public static double minCscore = Data.NO_FILTER;

    // Annotation Level Filter Options
    public static String functionInput = "";
    public static String geneInput = "";
    public static String transcriptFile = "";
    public static boolean isCcdsOnly = false;
    public static boolean isCanonicalOnly = false;
    public static String polyphenHumdiv = "probably,possibly,unknown,benign";
    public static String polyphenHumvar = "probably,possibly,unknown,benign";

    // Genotype Level Filter Options
    public static String sampleFile = "";
    public static boolean isAllSample = false;
    public static String evsSample = "";
    public static String evsMafPop = "all";
    public static boolean isAllNonRef = false;
    public static double maf = 0.5;
    public static double ctrlMaf = 0.5;
    public static double maf4Recessive = Data.NO_FILTER;
    public static double mhgf4Recessive = Data.NO_FILTER;
    public static int minCoverage = Data.NO_FILTER;
    public static int minCaseCoverageCall = Data.NO_FILTER;
    public static int minCaseCoverageNoCall = Data.NO_FILTER;
    public static int minCtrlCoverageCall = Data.NO_FILTER;
    public static int minCtrlCoverageNoCall = Data.NO_FILTER;
    public static int minVarPresent = 1; // special case
    public static int minCaseCarrier = Data.NO_FILTER;
    public static String[] varStatus; // null: no filer or all    
    public static double[] hetPercentAltRead = null; // {min, max}
    public static double[] homPercentAltRead = null;
    public static double genotypeQualGQ = Data.NO_FILTER;
    public static double strandBiasFS = Data.NO_FILTER;
    public static double haplotypeScore = Data.NO_FILTER;
    public static double rmsMapQualMQ = Data.NO_FILTER;
    public static double qualByDepthQD = Data.NO_FILTER;
    public static double qual = Data.NO_FILTER;
    public static double readPosRankSum = Data.NO_FILTER;
    public static double mapQualRankSum = Data.NO_FILTER;
    public static boolean isQcMissingIncluded = false;
    public static int maxQcFailSample = Data.NO_FILTER;

    // hidden option for slave / below two options needs to be removed soon
    public static boolean isFlipMaf = false;
    public static boolean isIncludeAllGeno = false;

}
