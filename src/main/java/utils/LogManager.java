package utils;

import com.github.lalyos.jfiglet.FigletFont;
import function.cohort.collapsing.CollapsingCommand;
import function.external.base.DataManager;
import global.Data;
import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author nick
 */
public class LogManager {

    private static BufferedWriter userLog = null;

    private static String runTime;

    // users command log file path
    private static final String USERS_COMMAND_LOG = Data.ATAV_HOME + "log/users.command.log";
    private static final String USERS_COMMAND_FAILED_LOG = Data.ATAV_HOME + "log/users.command.failed.log";

    // program start date
    private static final Date date = new Date();

    public static void run() {
        logRunTime();

        writeUserCommand2Log();

        close();
    }

    public static void initPath() {
        try {
            userLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    CommonCommand.outputPath + "atav.log")));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        try {
            writeAndPrintNoNewLine(FigletFont.convertOneLine("ATAV"));
            writeAndPrint("Version: " + Data.VERSION);
            writeAndPrint("Start: " + date.toString());
            writeAndPrint("Compute server: " + System.getenv("HOSTNAME"));

            writeLog("ATAV command:");
            writeLog(CommandManager.command + "\n");

            userLog.flush();

            // email user job running
            EmailManager.sendEmailToUser("ATAV Job (" + LogManager.getJobID() + ") Running", CommandManager.command);
        } catch (Exception e) {
            ErrorManager.print("Error in writing log file: " + e.toString(), ErrorManager.UNEXPECTED_FAIL);
        }
    }

    public static void writeAndPrint(String str) {
        System.out.println(str + "\n");
        writeLog(str + "\n");
    }

    public static void writeAndPrintNoNewLine(String str) {
        System.out.println(str);
        writeLog(str);
    }

    public static void writeLog(String str) {
        try {
            if (userLog != null) {
                userLog.write(str + "\n");
            }
        } catch (Exception e) {
        }
    }

    public static void close() {
        try {
            if (userLog != null) {
                userLog.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logRunTime() {
        long elapsedTime = RunTimeManager.getElapsedTime();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);

        runTime = seconds + " seconds "
                + "(aka " + minutes + " minutes or "
                + hours + " hours)";

        writeAndPrint("\n\nTotal runtime: " + runTime + "\n");
    }

    private static void writeUserCommand2Log() {
        logUserCommand(USERS_COMMAND_LOG, ErrorManager.SUCCESS);

        emailUserJobComplete("", ErrorManager.SUCCESS);
    }

    public static void writeUserCommand2FailedLog(String error, int exit) {
        logUserCommand(USERS_COMMAND_FAILED_LOG, exit);

        emailUserJobComplete(error, exit);
    }

    private static void logUserCommand(String logFilePath, int exit) {
        try {
            if (isBioinfoTeam()) {
                return;
            }

            File file = new File(logFilePath);

            FileWriter fileWritter = new FileWriter(file, true);

            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            String cmdLogStr = getCommandLogStr(exit);
            bufferWritter.write(cmdLogStr);
            bufferWritter.newLine();
            bufferWritter.close();
        } catch (Exception e) { // not output error when writing to log file denied

        }
    }

    private static void emailUserJobComplete(String error, int exit) {
        String cmdLogStr = getCommandLogStr(exit);

        String str = "Completed";
        if (exit != ErrorManager.SUCCESS) {
            str = "Failed";
            // hack to force send email when job failed to complete.
            CommonCommand.email = true;
        }

        // email user job complete
        EmailManager.sendEmailToUser("ATAV Job (" + LogManager.getJobID() + ") " + str, cmdLogStr + "\n\n" + error);
    }

    public static String getCommandLogStr(int exit) {
        long outputFolderSize = folderSize(new File(CommonCommand.realOutputPath));

        return Data.userName + "\t"
                + date.toString() + "\t"
                + DBManager.getHost() + "\t"
                + System.getenv("HOSTNAME") + "\t"
                + CommandManager.command + "\t"
                + runTime + "\t"
                + outputFolderSize + " bytes" + "\t"
                + System.getenv("JOB_ID") + "\t"
                + exit;
    }

    public static String getJobID() {
        return System.getenv("JOB_ID");
    }

    public static boolean isBioinfoTeam() {
        try {
            String configPath = Data.SYSTEM_CONFIG;

            if (CommonCommand.isDebug) {
                configPath = Data.SYSTEM_CONFIG_FOR_DEBUG;
            }

            InputStream input = new FileInputStream(configPath);
            Properties prop = new Properties();
            prop.load(input);

            String members = prop.getProperty("bioinfo-team");

            return members.contains(Data.userName);
        } catch (Exception e) {
            return false;
        }
    }

    private static long folderSize(File directory) {
        long length = 0;
        try {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += folderSize(file);
                }
            }
        } catch (Exception e) {
        }
        return length;
    }

    public static void logExternalDataVersion() {
        // collapsing lite is based on user input data
        if (CollapsingCommand.isCollapsingLite) {
            return;
        }

        String externalDataVersion = DataManager.getVersion();

        if (!externalDataVersion.isEmpty()) {
            writeAndPrintNoNewLine("External data version:");
            writeAndPrintNoNewLine(externalDataVersion);
        }
    }
}
