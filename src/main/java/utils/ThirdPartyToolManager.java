package utils;

import com.google.common.base.Stopwatch;
import function.cohort.base.CohortLevelFilterCommand;
import global.Data;
import function.cohort.base.SampleManager;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import static utils.LogManager.writeAndPrint;

/**
 *
 * @author nick
 */
public class ThirdPartyToolManager {

    public static String PYTHON;
    public static String PERL;
    public static String PLINK;
    public static String KING;
    public static String FLASHPCA;
    private static String RSCRIPT;
    private static final String COLLAPSED_REGRESSION_R = Data.ATAV_HOME + "lib/collapsed_regression_2.0.R";
    private static final String PVALS_QQPLOT_R = Data.ATAV_HOME + "lib/pvals_qqplot.R";
    private static final String PERM_QQPLOT_FOR_COLLAPSING = Data.ATAV_HOME + "lib/generate_qq.py";
    private static final String MANN_WHITNEY_TEST = Data.ATAV_HOME + "lib/mann_whitney_test.py";

    public static void init() {
        initDataFromSystemConfig();
    }

    private static void initDataFromSystemConfig() {
        try {
            String configPath = Data.SYSTEM_CONFIG;

            if (CommonCommand.isDebug) {
                configPath = Data.SYSTEM_CONFIG_FOR_DEBUG;
            }

            InputStream input = new FileInputStream(configPath);
            Properties prop = new Properties();
            prop.load(input);

            PYTHON = prop.getProperty("python");
            PERL = prop.getProperty("perl");
            PLINK = prop.getProperty("plink");
            KING = prop.getProperty("king");
            FLASHPCA = prop.getProperty("flashpca");
            RSCRIPT = prop.getProperty("rscript");
        } catch (IOException e) {
            ErrorManager.send(e);
        }
    }

    public static String systemCall(String[] cmd) {
        LogManager.writeAndPrintNoNewLine("System call start");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        int exitValue = Data.INTEGER_NA;
        StringBuilder cmdReadLine = new StringBuilder();

        try {
            Process process;

            if (cmd.length > 1) {
                LogManager.writeAndPrintNoNewLine(cmd[2]);
                process = Runtime.getRuntime().exec(cmd);
            } else {
                LogManager.writeAndPrintNoNewLine(cmd[0]);
                process = Runtime.getRuntime().exec(cmd[0]);
            }

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = stdInput.readLine()) != null) {
                cmdReadLine.append(line).append("\n");
            }
            
            while ((line = stdError.readLine()) != null) {
                cmdReadLine.append(line).append("\n");
            }

            exitValue = process.waitFor();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        stopwatch.stop();
        String runTime = getTotalRunTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));
      
        if (exitValue != 0) {
            writeAndPrint("System call failed: " + runTime);
            ErrorManager.print(cmdReadLine.toString(), ErrorManager.UNEXPECTED_FAIL);
            return Data.STRING_NA;
        } else {
            writeAndPrint("System call complete: " + runTime);
            return cmdReadLine.toString();
        }
    }

    private static String getTotalRunTime(long elapsedTime) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);

        return seconds + " seconds "
                + "(aka " + minutes + " minutes or "
                + hours + " hours)";
    }

    public static void callCollapsedRegression(String outputFile,
            String geneSampleMatrixFilePath,
            String method) {
        String cmd = RSCRIPT + " "
                + COLLAPSED_REGRESSION_R + " "
                + "--samples " + SampleManager.getTempCovarPath() + " "
                + "--clps " + geneSampleMatrixFilePath + " "
                + "--out " + outputFile + " "
                + "--method " + method + " "
                + "--transpose "
                + "--log " + CommonCommand.outputPath + "regress.log";

        systemCall(new String[]{cmd});
    }

    public static void callPvalueQQPlot(String pvalueFile, int col, String outputPath) {
        String cmd = RSCRIPT + " "
                + PVALS_QQPLOT_R + " "
                + pvalueFile + " "
                + col + " "
                + outputPath;

        systemCall(new String[]{cmd});
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

    public static void generateQQPlot4CollapsingFetP(String summaryFilePath, String matrixFilePath, String outputPath) {
        String cmd = PYTHON + " "
                + PERM_QQPLOT_FOR_COLLAPSING + " "
                + "--nprocs 10 "
                + summaryFilePath + " "
                + matrixFilePath + " "
                + outputPath; // output path

        systemCall(new String[]{cmd});
    }

    public static void gzipFile(String path) {
        String cmd = "gzip -9 " + path;

        systemCall(new String[]{cmd});
    }

    public static void runMannWhitneyTest(String genotypesFilePath) {
        String cmd = PYTHON + " "
                + MANN_WHITNEY_TEST + " "
                + genotypesFilePath + " "
                + SampleManager.getExistingSampleFile();

        systemCall(new String[]{cmd});
    }
}
