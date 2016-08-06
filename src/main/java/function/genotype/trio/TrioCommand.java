package function.genotype.trio;

import function.external.evs.EvsCommand;
import function.external.exac.ExacCommand;
import function.external.knownvar.KnownVarCommand;
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

    public static boolean isRunTier = false;

    public static void initDenovoOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--include-noflag":
                    isIncludeNoflag = true;
                    break;
                case "--run-tier":
                    isRunTier = true;
                    EvsCommand.isIncludeEvs = true;
                    ExacCommand.isIncludeExac = true;
                    KnownVarCommand.isIncludeKnownVar = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }

    public static void initCompHetOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--combfreq":
                case "--comb-freq":
                    checkValueValid(1, 0, option);
                    combFreq = getValidDouble(option);
                    break;
                case "--include-noflag":
                    isIncludeNoflag = true;
                    break;
                case "--run-tier":
                    isRunTier = true;
                    EvsCommand.isIncludeEvs = true;
                    ExacCommand.isIncludeExac = true;
                    KnownVarCommand.isIncludeKnownVar = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }

    public static boolean isCombFreqValid(double value) {
        if (combFreq == Data.NO_FILTER) {
            return true;
        }

        return value <= combFreq;
    }
}
