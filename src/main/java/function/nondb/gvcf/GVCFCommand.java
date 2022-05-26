package function.nondb.gvcf;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class GVCFCommand {

    public static boolean isRun = false;
    public static String gvcfFilePath = Data.STRING_NA;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--gvcf":
                    gvcfFilePath = getValidPath(option);
                    if (!gvcfFilePath.endsWith(".gvcf.gz")) {
                        ErrorManager.print("Not a valid GVCF file: "
                                + gvcfFilePath, ErrorManager.INPUT_PARSING);
                    }
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
