package utils;

import global.Data;
import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nick
 */
public class LogManager {

    private static BufferedWriter userLog = null;
    private static StringBuilder basicInfo = new StringBuilder();

    public static String runTime;

    // users command log file path
    public static final String USERS_COMMAND_LOG = "/nfs/goldstein/software/atav_home/log/users.command.log";

    public static void initBasicInfo() {
        basicInfo.append("\n\n");
        basicInfo.append("Software:\t\t" + Data.AppTitle + "\n");
        basicInfo.append("Version:\t\t" + Data.version + "\n");
        basicInfo.append("Lead Developer:\t\t" + Data.leadDeveloper + "\n");
        basicInfo.append("Pipeline Developer:\t" + Data.pipelineDeveloper + "\n");
        basicInfo.append("Project Manager:\t" + Data.projectManager + "\n");
        basicInfo.append("Past Developer:\t\t" + Data.pastDeveloper + "\n");
        basicInfo.append("Year:\t\t\t" + Data.year + "\n");
        basicInfo.append("Institute:\t\t" + Data.insititue + "\n");
    }

    public static void initPath() {
        try {
            userLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    CommonCommand.outputPath + "atav.log")));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LogManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        Data.userName = System.getProperty("user.name");
        try {
            Date date = new Date();
            writeLog("The following job was run on " + date.toString() + ".\n");
            writeAndPrint(basicInfo.toString());
            writeLog(Data.userName + " is running ATAV with the following command:");
            writeLog(CommandManager.command + "\n");

            writeAndPrint("ATAV news: "
                    + "http://redmine.igm.cumc.columbia.edu/projects/atav/news");

            writeAndPrint("ATAV wiki: "
                    + "http://redmine.igm.cumc.columbia.edu/projects/atav/wiki");

            // write and reopen
            userLog.close();
            userLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    CommonCommand.outputPath + "atav.log", true)));

        } catch (Exception e) {
            ErrorManager.print("Error in writing log file: " + e.toString());
        }
    }

    public static void writeAndPrint(String str) {
        System.out.println(str + "\n");
        writeLog(str + "\n");
    }

    public static void writeAndPrintWithoutNewLine(String str) {
        System.out.println(str);
        writeLog(str);
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

    public static void logRunTime() {
        long elapsedTime = RunTimeManager.getElapsedTime();
        
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);

        runTime = seconds + " seconds "
                + "(aka " + minutes + " minutes or "
                + hours + " hours)";

        writeAndPrint("\nTotal runtime: "
                + runTime + "\n");
    }

    public static void logUserCommand() {
        try {
            if (isBioinfoTeam()) {
                return;
            }

            File file = new File(USERS_COMMAND_LOG);

            FileWriter fileWritter = new FileWriter(file, true);

            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            Date date = new Date();

            long outputFolderSize = folderSize(new File(CommonCommand.realOutputPath));

            bufferWritter.write(Data.userName + "\t"
                    + date.toString() + "\t"
                    + DBManager.getHost() + "\t"
                    + System.getenv("HOSTNAME") + "\t"
                    + CommandManager.command + "\t"
                    + runTime + "\t"
                    + outputFolderSize + " bytes");

            bufferWritter.newLine();

            bufferWritter.close();
        } catch (Exception e) { // not output error when writing to log file denied

        }
    }

    private static boolean isBioinfoTeam() throws Exception {
        InputStream input = new FileInputStream(Data.SYSTEM_CONFIG);
        Properties prop = new Properties();
        prop.load(input);

        String members = prop.getProperty("bioinfo-team");

        if (members.contains(Data.userName)) {
            return true;
        }

        return false;
    }

    private static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += folderSize(file);
            }
        }
        return length;
    }
}
