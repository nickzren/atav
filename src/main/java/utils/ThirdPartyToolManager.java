package utils;

import function.external.flanking.FlankingCommand;
import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;
import function.genotype.base.SampleManager;
import function.variant.base.VariantLevelFilterCommand;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 *
 * @author nick
 */
public class ThirdPartyToolManager {

    private static final String R_301_SCRIPT_SYSTEM_PATH = "/nfs/goldstein/software/R-3.0.1/bin/Rscript";
    private static final String R_325_SCRIPT_SYSTEM_PATH = "/nfs/goldstein/software/R-3.2.5/bin/Rscript";
    private static final String COLLAPSED_REGRESSION_R = "/nfs/goldstein/software/atav_home/lib/collapsed_regression_2.0.R";
    private static final String PVALS_QQPLOT_R = "/nfs/goldstein/software/atav_home/lib/pvals_qqplot.R";
    private static final String QQPLOT_FOR_COLLAPSING_R = "/nfs/goldstein/software/atav_home/lib/qqplot_for_collapsing.R";
    private static final String PERL_SYSTEM_PATH = "perl";
    private static final String FLANKING_SEQ_PERL = "/nfs/goldstein/software/atav_home/lib/flanking_seq.pl";
    private static final String TRIO_DENOVO_TIER = "/nfs/goldstein/software/atav_home/lib/r0.5_trio_denovo_tier.R";
    private static final String TRIO_COMP_HET_TIER = "/nfs/goldstein/software/atav_home/lib/r0.5_trio_comp_het_tier.R";
    private static final String NON_TRIO_TIER = "/nfs/goldstein/software/atav_home/lib/non_trio_tier.R";
    public static final String PYTHON = "/nfs/goldstein/software/python2.7.7/bin/python";
    public static final String PLINK = "/nfs/goldstein/software/PLINK_1.90/3.38/plink";
    public static final String KING = "/nfs/goldstein/software/king_relatedness/king";

    public static int systemCall(String[] cmd) {
        LogManager.writeAndPrintNoNewLine("System call start");

        int exitValue = Data.INTEGER_NA;

        try {
            Process myProc;

            if (cmd.length > 1) {
                LogManager.writeAndPrintNoNewLine(cmd[2]);
                myProc = Runtime.getRuntime().exec(cmd);
            } else {
                LogManager.writeAndPrintNoNewLine(cmd[0]);
                myProc = Runtime.getRuntime().exec(cmd[0]);
            }
            InputStream is = myProc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line;

            Vector<String> result = new Vector<>();
            while ((line = br.readLine()) != null) {
                result.add(line);
            }

            exitValue = myProc.waitFor();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        if (exitValue != 0) {
            LogManager.writeAndPrint("System call failed.");
        } else {
            LogManager.writeAndPrint("System call complete.");
        }

        return exitValue;
    }

    public static void callCollapsedRegression(String outputFile,
            String geneSampleMatrixFilePath,
            String method) {
        String cmd = R_301_SCRIPT_SYSTEM_PATH + " "
                + COLLAPSED_REGRESSION_R + " "
                + "--samples " + SampleManager.getTempCovarPath() + " "
                + "--clps " + geneSampleMatrixFilePath + " "
                + "--out " + outputFile + " "
                + "--method " + method + " "
                + "--transpose "
                + "--log " + CommonCommand.outputPath + "regress.log";

        int exitValue = systemCall(new String[]{cmd});

        if (exitValue != 0) {
            LogManager.writeAndPrint("\nwarning: the application failed to run Collapsed "
                    + "Regression script (" + method + "). \n");

            deleteFile(outputFile);
        }
    }

    public static void callFlankingSeq(String baseFlankingSeqFilePath) {
        String cmd = PERL_SYSTEM_PATH + " " + FLANKING_SEQ_PERL
                + " --variant " + VariantLevelFilterCommand.includeVariantId
                + " --width " + FlankingCommand.width
                + " --out " + baseFlankingSeqFilePath;

        int exitValue = systemCall(new String[]{cmd});

        if (exitValue != 0) {
            LogManager.writeAndPrint("\nwarning: the application failed to run flanking "
                    + "sequence script. \n");

            deleteFile(baseFlankingSeqFilePath);
        }
    }

    public static void callPvalueQQPlot(String pvalueFile, int col, String outputPath) {
        String cmd = R_301_SCRIPT_SYSTEM_PATH + " "
                + PVALS_QQPLOT_R + " "
                + pvalueFile + " "
                + col + " "
                + outputPath;

        int exitValue = systemCall(new String[]{cmd});

        if (exitValue != 0) {
            deleteFile(outputPath);
        }
    }

    public static void generatePvaluesQQPlot(String title, String pvalueName,
            String pvalueFile, String outputPath) {
        String[] temp = title.split(",");

        int col = 0;

        for (String str : temp) {
            col++;

            if (str.trim().equalsIgnoreCase(pvalueName)) {
                break;
            }
        }

        callPvalueQQPlot(pvalueFile, col, outputPath);
    }

    public static void generateQQPlot4CollapsingFetP(String matrixFilePath, String outputPath) {
        String cmd = R_325_SCRIPT_SYSTEM_PATH + " "
                + QQPLOT_FOR_COLLAPSING_R + " "
                + GenotypeLevelFilterCommand.sampleFile + " "
                + matrixFilePath + " "
                + "1000 " // permutation#
                + outputPath; // output path

        int exitValue = systemCall(new String[]{cmd});

        if (exitValue != 0) {
            deleteFile(outputPath);
        }

    }

    private static void deleteFile(String filePath) {
        File f = new File(filePath);
        f.deleteOnExit();
    }

    public static void gzipFile(String path) {
        String cmd = "gzip -9 " + path;

        systemCall(new String[]{cmd});
    }

    public static void runTrioDenovoTier(String denovoFilePath) {
        String cmd = R_325_SCRIPT_SYSTEM_PATH + " "
                + TRIO_DENOVO_TIER + " "
                + denovoFilePath;

        systemCall(new String[]{cmd});
    }

    public static void runTrioCompHetTier(String compHetFilePath) {
        String cmd = R_325_SCRIPT_SYSTEM_PATH + " "
                + TRIO_COMP_HET_TIER + " "
                + compHetFilePath;

        systemCall(new String[]{cmd});
    }

    public static void runNonTrioTier(String variantFilePath) {
        String cmd = R_325_SCRIPT_SYSTEM_PATH + " "
                + NON_TRIO_TIER + " "
                + variantFilePath;

        systemCall(new String[]{cmd});
    }
}
