package utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author nick
 */
public class ErrorManager {

    private final static String newLine = "\n";

    public static void send(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        print(newLine + sw.toString());
    }

    public static void print(String msg) {
        LogManager.writeAndPrint(msg + "\n\nExit\n");
        LogManager.close();
        System.exit(1);
    }
}
