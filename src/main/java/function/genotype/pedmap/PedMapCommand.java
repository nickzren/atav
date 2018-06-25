package function.genotype.pedmap;

import java.util.Iterator;
import static utils.CommandManager.getValidFloat;
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
    public static float kinshipRelatednessThreshold = 0.0884f;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
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
                case "--kinship-relatedness-threshold":
                    kinshipRelatednessThreshold = getValidFloat(option);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
