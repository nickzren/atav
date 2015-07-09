package function.genotype.family;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class FamilyCommand {

    public static boolean isFamilyAnalysis = false;
    public static String familyId = "";
    public static double popCtrlMaf = Data.NO_FILTER;

    
    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--family-id")) { // need change here
                familyId = getValidPath(option);
                FamilyManager.initFamilyIdList();
            } else if (option.getName().equals("--pop-ctrl-maf")) {
                checkValueValid(0.5, 0, option);
                popCtrlMaf = getValidDouble(option);
            } else if (option.getName().equals("--pop-ctrl-maf-rec")) {
                checkValueValid(0.5, 0, option);
                CommonCommand.maf4Recessive = getValidDouble(option);
            } else if (option.getName().equals("--pop-ctrl-mhgf-rec")) {
                checkValueValid(0.5, 0, option);
                CommonCommand.mhgf4Recessive = getValidDouble(option);
            } else {
                continue;
            }

            iterator.remove();
        }

        if (familyId.isEmpty()) {
            ErrorManager.print("Please specify a family id by using '--family-id'.");
        }
    }
}
