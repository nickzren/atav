package utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author nick
 */
public class ErrorManager {

    public final static int SUCCESS = 0;
    public final static int UNEXPECTED_FAIL = 1;
    public final static int COMMAND_PARSING = 2;
    public final static int INPUT_PARSING = 3;
    public final static int MAX_CONNECTION = 4;
    public final static int NET_WRITE_TIMEOUT = 99;
    public final static int COMMUNICATIONS_LINK_FAILURE = 99;
    public final static int LOCK_WAIT_TIMEOUT = 99;

    public static void send(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        int exit = UNEXPECTED_FAIL;
        if (sw.toString().contains("net_write_timeout")) { // hack here since we have no clues yet how it happened
            exit = NET_WRITE_TIMEOUT;
        } else if (sw.toString().contains("Communications link failure")) { // hack here since we have no clues yet how it happened
            exit = COMMUNICATIONS_LINK_FAILURE;
        } else if (sw.toString().contains("Lock wait timeout ")) { // hack here since we have no clues yet how it happened
            exit = LOCK_WAIT_TIMEOUT;
        }

        if (!LogManager.isBioinfoTeam()) {
            String cmdLogStr = LogManager.getCommandLogStr(exit);
            EmailManager.sendEmailToATAVMail("ATAV Job Failed",
                    cmdLogStr + "\n\n" + sw.toString());
        }

        print("\n" + sw.toString(), exit);
    }

    public static void print(String msg, int exit) {
        LogManager.writeUserCommand2FailedLog(msg, exit);
        LogManager.writeAndPrint(msg + "\n\nExit\n");
        LogManager.close();
        System.exit(exit);
    }
}
