package function.nondb.dpbin;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
/**
 *
 * @author nick
 */
public class DPBinCommand {
    public static boolean isRun = false;
    public static String dpBinFilePath = Data.STRING_NA;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--dp-bin-file":
                    dpBinFilePath = getValidPath(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
