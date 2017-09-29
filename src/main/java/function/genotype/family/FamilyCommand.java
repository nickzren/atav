package function.genotype.family;

import java.util.Iterator;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class FamilyCommand {

    public static boolean isFamilyAnalysis = false;
    public static String familyId = "";

    
    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--family-id")) { // need change here
                familyId = getValidPath(option);
                FamilyManager.initFamilyIdSet();
            } else {
                continue;
            }

            iterator.remove();
        }

        if (familyId.isEmpty()) {
            ErrorManager.print("Please specify a family id by using '--family-id'.", ErrorManager.COMMAND_PARSING);
        }
    }
}
