package function.genotype.trio;

import function.external.evs.EvsCommand;
import function.external.exac.ExacCommand;
import function.external.knownvar.KnownVarCommand;
import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class TrioCommand {

    public static boolean isTrioDenovo = false;
    public static boolean isTrioCompHet = false;
    public static boolean isRunTier = false;

    public static void initDenovoOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
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
}
