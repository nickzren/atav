package function.cohort.singleton;

import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class SingletonCommand {

    public static boolean isList = false;
    public static boolean isMannWhitneyTest = false;
    public static boolean isExcludeNoFlag = false;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--mann-whitney-test":
                    isMannWhitneyTest = true;
                    break;
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
