package atav.manager.utils;

import atav.global.Data;
import java.io.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nick
 */
public class LogManager {

    private static BufferedWriter userLog = null;
    private static StringBuilder basicInfo = new StringBuilder();

    public static void initBasicInfo() {
        basicInfo.append("\n+---------------------------------------------------------------+\n");
        basicInfo.append("| [Software]:\t" + Data.AppTitle + "\t|\n");
        basicInfo.append("  [Version]:\t" + Data.version + "\t\t\t\t\t\t\n");
        basicInfo.append("| [Developers]:\t" + Data.developer + "\t\t\t\t|\n");
        basicInfo.append("  [Year]:\t" + Data.year + "\t\t\t\t\t\t\n");
        basicInfo.append("| [Institute]:\t"+ Data.insititue + "\t\t|\n");
        basicInfo.append("+---------------------------------------------------------------+");
    }    

    public static void initPath() {
        try {
            userLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    CommandValue.outputPath + "atav.log")));
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
                    + "http://redmine2.chgv.lsrc.duke.edu/redmine/projects/atav/news");

            writeAndPrint("ATAV manual: "
                    + "http://redmine2.chgv.lsrc.duke.edu/redmine/projects/atav/wiki");

            // write and reopen
            userLog.close();
            userLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    CommandValue.outputPath + "atav.log", true)));

        } catch (Exception e) {
            ErrorManager.print("Error in writing log file: " + e.toString());
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

        System.exit(0);
    }
}
