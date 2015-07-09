package utils;

import global.Data;

/**
 *
 * @author nick
 */
public class CommonCommand {

    // common values
    public static boolean isDebug = false;
    // public static boolean isDebug = true;

    public static boolean isNonDBAnalysis = false;
    public static boolean isNonSampleAnalysis = false;
    
    public static String outputPath = "";
    public static String realOutputPath = "";
    public static String outputDirName = "";

    public static String regionInput = ""; // either a region or a region file path.

    // Variant Level Filter Options
    public static String includeVariantId = "";
    public static String excludeVariantId = "";
    public static boolean isExcludeArtifacts = false;
    public static boolean isExcludeSnv = false;
    public static boolean isExcludeIndel = false;
    public static String evsMafPop = "all";
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
    public static boolean isAllNonRef = false;
    public static String evsSample = "";
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
}
