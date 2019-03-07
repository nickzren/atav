package function.test;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class TestCommand {
    public static boolean isTest = false;
    public static int sampleID = Data.INTEGER_NA;
    public static String dpBinFilePath = Data.STRING_NA;
    
     public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--dp-bin-file-path":
                    dpBinFilePath = getValidPath(option);
                    break;
                case "--sample-id":
                    sampleID = getValidInteger(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
