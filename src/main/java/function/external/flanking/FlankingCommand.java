package function.external.flanking;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidInteger;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class FlankingCommand {

    public static boolean isListFlankingSeq = false;
    public static int width = 0;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--width")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                width = getValidInteger(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }
}
