package function.cohort.trio;

import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class TrioCommand {

    public static boolean isList = false;
    public static boolean isExcludeDUO = false;
    
    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--exclude-duo":
                    isExcludeDUO = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
