package atav.manager.utils;

import atav.global.Data;

/**
 *
 * @author nick
 */
public class CommandValue {

    // common values
    public static boolean isDebug = false;
    //    public static boolean isDebug = true;
    public static String outputPath = "";
    public static String realOutputPath = "";
    public static String outputDirName = "";
    // variant genotype base analysis function
    public static boolean isPedMap = false;
    public static boolean isFisher = false;
    public static boolean isCollapsingSingleVariant = false;
    public static boolean isCollapsingCompHet = false;
    public static boolean isListVarGeno = false;
    public static boolean isTrioDenovo = false;
    public static boolean isTrioCompHet = false;
    public static boolean isSiblingCompHet = false;
    public static boolean isFamilyAnalysis = false;
    public static boolean isParentalMosaic = false;
    public static boolean isLinear = false;

    public static boolean isListNewVarId = false;

    // variant annotation base analysis function
    public static boolean isListVarAnno = false;
    public static boolean isListGeneDx = false;

    // sample coverage base analysis function
    public static boolean isCoverageSummary = false;
    public static boolean isSiteCoverageSummary = false;
    public static boolean isCoverageSummaryPipeline = false;
    public static boolean isCoverageComparison = false;

    // data filter
    public static String functionInput = "";
    public static String sampleFile = "";
    public static boolean isAllSample = false;
    public static String variantInputFile = "";
    public static String includeVariantInput = "";
    public static String excludeVariantInput = "";
    public static String geneInput = "";
    public static String geneBoundariesFile = "";
    public static String transcriptFile = "";
    public static String regionInput = ""; // either a region or a region file path.
    public static String covariateFile = "";
    public static String quantitativeFile = "";
    public static boolean isExcludeSnv = false;
    public static boolean isExcludeIndel = false;
    public static boolean isIncludeAllGeno = false;
    public static boolean isExcludeArtifacts = false;

    // quality filter
    public static double maf = 0.5;
    public static double ctrlMaf = 0.5;
    public static double popCtrlMaf = Data.NO_FILTER;
    public static double looMaf = Data.NO_FILTER;
    public static double maf4Recessive = Data.NO_FILTER;
    public static double mhgf4Recessive = Data.NO_FILTER;
    public static String polyphenHumdiv = "probably,possibly,unknown,benign";
    public static String polyphenHumvar = "probably,possibly,unknown,benign";
    public static double[] hetPercentAltRead = null; // {min, max}
    public static double[] homPercentAltRead = null;
    public static String evsSample = "";
    public static String evsMafPop = "all";
    public static double evsMaf = Data.NO_FILTER;
    public static double evsMhgf4Recessive = Data.NO_FILTER;
    public static boolean isOldEvsUsed = false;
    public static boolean isExcludeEvsQcFailed = false;

    public static String exacPop = "global";
    public static float exacMaf = Data.NO_FILTER;
    public static float exacVqslodSnv = Data.NO_FILTER;
    public static float exacVqslodIndel = Data.NO_FILTER;

    public static int minCoverage = Data.NO_FILTER;
    public static int minCaseCoverageCall = Data.NO_FILTER;
    public static int minCaseCoverageNoCall = Data.NO_FILTER;
    public static int minCtrlCoverageCall = Data.NO_FILTER;
    public static int minCtrlCoverageNoCall = Data.NO_FILTER;
    public static int minCoverageRecessive = Data.NO_FILTER;
    public static boolean isCcdsOnly = false;
    public static boolean isCanonicalOnly = false;
    public static double genotypeQualGQ = Data.NO_FILTER;
    public static double strandBiasFS = Data.NO_FILTER;
    public static double haplotypeScore = Data.NO_FILTER;
    public static double rmsMapQualMQ = Data.NO_FILTER;
    public static double qualByDepthQD = Data.NO_FILTER;
    public static double qual = Data.NO_FILTER;
    public static double readPosRankSum = Data.NO_FILTER;
    public static double mapQualRankSum = Data.NO_FILTER;
    public static String[] varStatus; // null: no filer or all
    public static int minVarPresent = 1; // special case
    public static int minCaseCarrier = Data.NO_FILTER;
    public static int minHomCaseRec = Data.NO_FILTER; // collasping, fisher, linear
    public static double minCscore = Data.NO_FILTER;
    public static boolean isQcMissingIncluded = false;

    // ped map
    public static boolean isVariantIdOnly = false;
    public static boolean isCombineMultiAlleles = false;
    public static boolean isEigenstrat = false;
    public static boolean useGroup = false;
    public static String GroupType = "control";
    public static String pedMapPath = "";
    public static double dmaf = 0.5;   // 0.01;

    // fisher 
    public static double threshold4Sort = Data.NO_FILTER;
    public static boolean isCaseOnly = false; // also listvargeno

    // list var geno
    public static boolean isAllNonRef = false;

    // collapsing
    public static boolean isRecessive = false;
    public static String[] models = {"allelic", "dominant", "recessive", "genotypic"};
    public static double varMissingRate = Double.MAX_VALUE;
    public static boolean isGeneCoverage = false;
    public static String coverageSummaryFile = "";
    public static double looCombFreq = Data.NO_FILTER;
    public static boolean isCollapsingDoLinear = false;
    public static boolean isCollapsingDoLogistic = false;

    // trio denovo
    public static boolean isIncludeNoflag = false;

    // comp het
    public static double combFreq = Data.NO_FILTER;

    // family analysis
    public static String familyId = "";

    // coverage summary
    public static String coveredRegionFile = "";
    public static double minPercentRegionCovered = 0; //so all is output by default 
    public static boolean isExcludeUTR = false;
    public static boolean isByExon = false;
    public static boolean isTerse = false;
    public static double exonCleanCutoff = -1; //not used by default
    public static double geneCleanCutoff = 1.0;

    // coverage comparison 
    public static boolean isCoverageComparisonDoLinear = false;
    public static double ExonMaxCovDiffPValue = 0.0;
    public static double ExonMaxPercentVarExplained = 100.0;

    // annotools
    public static boolean isNonSampleAnalysis = false;
    public static boolean isListFlankingSeq = false;
    public static int width = 0;
    public static boolean isJonEvsTool = false;
    public static String jonEvsInput = "";

    // list known var
    public static boolean isListKnownVar = false;
    public static int snvWidth = 2;
    public static int indelWidth = 9;

    // parental mosaic
    public static double probandQD = Data.NO_FILTER;
    public static double[] probandHetPercentAltRead = null;
    public static double probandBinomial = Data.NO_FILTER;

    // list evs
    public static boolean isListEvs = false;

    // list exac
    public static boolean isListExac = false;

    // hidden option for slave
    public static boolean isFlipMaf = false;
}
