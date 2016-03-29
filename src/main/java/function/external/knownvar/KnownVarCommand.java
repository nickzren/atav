package function.external.knownvar;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidInteger;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class KnownVarCommand {

    public static boolean isListKnownVar = false;
    public static boolean isKnownVarOnly = false;
    public static int snvWidth = 2;
    public static int indelWidth = 9;
    
    public static boolean isIncludeKnownVar = false;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--snv-width")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                snvWidth = getValidInteger(option);
            } else if (option.getName().equals("--indel-width")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                indelWidth = getValidInteger(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }
}
