package function.cohort.family;

import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class FamilyCommand {
    public static boolean isList = false;
    public static String inputFamilyId = "";

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--family-id":
                    inputFamilyId = option.getValue();
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
