package function.genotype.trio;

import function.external.denovo.DenovoDBCommand;
import function.external.evs.EvsCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.trap.TrapCommand;
import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class TrioCommand {

    public static boolean isListTrio = false;
    public static boolean isRunTier = false;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--run-tier":
                    isRunTier = true;
                    EvsCommand.isIncludeEvs = true;
                    KnownVarCommand.isIncludeKnownVar = true;
                    TrapCommand.isIncludeTrap = true;
                    DenovoDBCommand.isIncludeDenovoDB = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
