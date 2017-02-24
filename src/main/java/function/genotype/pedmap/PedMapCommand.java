package function.genotype.pedmap;

import java.util.Iterator;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class PedMapCommand {

    public static boolean isPedMap = false;
    public static boolean isVariantIdOnly = false;
    public static boolean isEigenstrat = false;
    public static boolean isKinship = false;
    public static String pedMapPath = "";
    public static String sampleCoverageSummaryPath = "";
    public static int seed = 42;

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
                case "--kinship":
                    isKinship = true;
                    break;
                case "--sample-coverage-summary":
                    sampleCoverageSummaryPath = getValidPath(option);
                    break;
                case "--seed":
                    seed = getValidInteger(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
