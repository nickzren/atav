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
            if (option.getName().equals("--variant-id-only")) {
                isVariantIdOnly = true;
            } else if (option.getName().equals("--eigenstrat")) {
                isEigenstrat = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }
}
