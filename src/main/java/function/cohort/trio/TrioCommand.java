package function.cohort.trio;

import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class TrioCommand {

    public static boolean isList = false;
    public static boolean isExcludeNoFlag = false;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--exclude-no-flag":
                    isExcludeNoFlag = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
