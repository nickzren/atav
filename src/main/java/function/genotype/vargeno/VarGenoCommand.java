package function.genotype.vargeno;

import function.external.evs.EvsCommand;
import function.external.exac.ExacCommand;
import function.external.knownvar.KnownVarCommand;
import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class VarGenoCommand {

    public static boolean isListVarGeno = false;
    public static boolean isRunTier = false;
    public static boolean isCaseOnly = false;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--case-only":
                    isCaseOnly = true;
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
}
