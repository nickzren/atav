package utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author nick
 */
public class ErrorManager {

    private final static String newLine = "\n";

    public final static int SUCCESS = 0;
    public final static int UNEXPECTED_FAIL = 1;
    public final static int COMMAND_PARSING = 2;
    public final static int INPUT_PARSING = 3;
    public final static int MAX_CONNECTION = 4;
    public final static int NET_WRITE_TIMEOUT = 99;

    public static void send(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        if (sw.toString().contains("net_write_timeout")) { // hack here since we have no clues yet how it happened
            print(newLine + sw.toString(), NET_WRITE_TIMEOUT);
        } else {
            print(newLine + sw.toString(), UNEXPECTED_FAIL);
        }
    }

    public static void print(String msg, int exit) {
        LogManager.writeUserCommand2FailedLog(exit);
        LogManager.writeAndPrint(msg + "\n\nExit\n");
        LogManager.close();
        System.exit(exit);
    }
}
