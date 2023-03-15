package function.cohort.singleton;

import java.util.Iterator;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class SingletonCommand {

    public static boolean isList = false;
    public static boolean isMannWhitneyTest = false;
    public static boolean isPhenolyzer = false;
    public static String phenolyzerPhenotypePath = "";

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--mann-whitney-test":
                    isMannWhitneyTest = true;
                    break;
                case "--phenolyzer-ph":
                    isPhenolyzer = true;
                    phenolyzerPhenotypePath = getValidPath(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
