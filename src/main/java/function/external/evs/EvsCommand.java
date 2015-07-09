package function.external.evs;

import java.util.Iterator;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
import utils.CommonCommand;

/**
 *
 * @author nick
 */
public class EvsCommand {

    // list evs
    public static boolean isListEvs = false;

    // jon evs tool
    public static boolean isJonEvsTool = false;
    public static String jonEvsInput = "";

    public static void initJonEvsToolOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--jon-evs-input")) {
                jonEvsInput = getValidPath(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }
}
