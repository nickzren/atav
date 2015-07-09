package utils;

import function.external.flanking.FlankingCommand;
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

    private static final String R_SCRIPT_SYSTEM_PATH = "/nfs/goldstein/software/R-3.0.1/bin/Rscript";
    private static final String COLLAPSED_REGRESSION_R = "/nfs/goldstein/software/atav_home/lib/collapsed_regression_2.0.R";
    private static final String PVALS_QQPLOT_R = "/nfs/goldstein/software/atav_home/lib/pvals_qqplot.R";
    private static final String PERL_SYSTEM_PATH = "perl";
    private static final String FLANKING_SEQ_PERL = "/nfs/goldstein/software/atav_home/lib/flanking_seq.pl";

    public static int systemCall(String[] cmd) {
        LogManager.writeAndPrintNoNewLine("System call start...");

        int exitValue = Data.NA;

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

            Vector<String> result = new Vector<String>();
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
        String cmd = R_SCRIPT_SYSTEM_PATH + " "
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
        String cmd = R_SCRIPT_SYSTEM_PATH + " "
                + PVALS_QQPLOT_R + " "
                + pvalueFile + " "
                + col + " "
                + outputPath;

        int exitValue = systemCall(new String[]{cmd});

        if (exitValue != 0) {
            LogManager.writeAndPrint("\nwarning: the application failed to run p value "
                    + "qq plot script. \n");

            deleteFile(outputPath);
        }
    }

    public static void generatePvaluesQQPlot(String title, String pvalueName,
            String pvalueFile, String outputPath) {
        String[] temp = title.split(",");

        int col = 0;

        for (String str : temp) {
            col++;

            if (str.equals(pvalueName)) {
                break;
            }
        }

        callPvalueQQPlot(pvalueFile, col, outputPath);
    }

    private static void deleteFile(String filePath) {
        File f = new File(filePath);
        f.deleteOnExit();
    }

    public static void gzipFile(String path) {
        String cmd = "gzip -9 " + path;

        systemCall(new String[]{cmd});
    }
}
