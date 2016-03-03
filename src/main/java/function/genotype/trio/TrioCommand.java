package function.genotype.trio;

import function.external.rvis.RvisCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidDouble;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class TrioCommand {

    // trio denovo
    public static boolean isTrioDenovo = false;
    public static boolean isIncludeNoflag = false;

    // trio comp het
    public static boolean isTrioCompHet = false;
    public static double combFreq = Data.NO_FILTER;

    public static void initDenovoOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--include-noflag")) {
                isIncludeNoflag = true;
            } else if (option.getName().equals("--include-rvis")) {
                RvisCommand.isIncludeRvis = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    public static void initCompHetOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--combfreq")
                    || option.getName().equals("--comb-freq")) {
                checkValueValid(1, 0, option);
                combFreq = getValidDouble(option);
            } else if (option.getName().equals("--include-noflag")) {
                isIncludeNoflag = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    public static boolean isCombFreqValid(double value) {
        if (combFreq == Data.NO_FILTER) {
            return true;
        }

        if (value <= combFreq) {
            return true;
        }

        return false;
    }
}
