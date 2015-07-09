package function.genotype.vargeno;

import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class VarGenoCommand {

    public static boolean isListVarGeno = false;

    public static boolean isCaseOnly = false;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--case-only")) {
                isCaseOnly = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }
}
