package function.genotype.pedmap;

import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class PedMapCommand {

    public static boolean isPedMap = false;
    public static boolean isVariantIdOnly = false;
    public static boolean isEigenstrat = false;
    public static String pedMapPath = "";

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--variant-id-only":
                    isVariantIdOnly = true;
                    break;
                case "--eigenstrat":
                    isEigenstrat = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
