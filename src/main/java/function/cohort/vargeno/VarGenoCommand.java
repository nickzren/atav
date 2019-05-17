package function.cohort.vargeno;

import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class VarGenoCommand {

    public static boolean isListVarGeno = false;
    public static boolean isMannWhitneyTest = false;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--mann-whitney-test":
                    isMannWhitneyTest = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
