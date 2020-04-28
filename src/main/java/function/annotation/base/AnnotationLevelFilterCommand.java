package function.annotation.base;

import function.external.primateai.PrimateAICommand;
import function.external.revel.RevelCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class AnnotationLevelFilterCommand {

    public static String effectInput = "";
    public static String geneInput = "";
    public static String geneBoundaryFile = "";
    public static boolean isCcdsOnly = false;
    public static boolean isCanonicalOnly = false;
    public static String polyphenHumdiv = Data.NO_FILTER_STR;
    public static String polyphenHumvar = Data.NO_FILTER_STR;
    public static boolean ensembleMissense = false;
    private static final String ensembleMissenseMsg = "--ensemble-missense applied "
            + ", it required to use --polyphen-humdiv, --min-revel-score and --min-primate-ai";

    public static final String[] POLYPHEN_CAT = {"probably", "possibly", "unknown", "benign"};

    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--effect":
                    effectInput = option.getValue();
                    break;
                case "--gene":
                    geneInput = option.getValue();
                    break;
                case "--gene-boundaries":
                case "--gene-boundary":
                    geneBoundaryFile = getValidPath(option);
                    break;
                case "--ccds-only":
                    isCcdsOnly = true;
                    break;
                case "--canonical-only":
                    isCanonicalOnly = true;
                    break;
                case "--polyphen":
                case "--polyphen-humdiv":
                    checkValuesValid(POLYPHEN_CAT, option);
                    polyphenHumdiv = option.getValue();
                    break;
                case "--polyphen-humvar":
                    checkValuesValid(POLYPHEN_CAT, option);
                    polyphenHumvar = option.getValue();
                    break;
                case "--ensemble-missense":
                    ensembleMissense = true;
                    LogManager.writeAndPrint(ensembleMissenseMsg);
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
        
        checkEnsembleMissenseValid();
    }

    private static void checkEnsembleMissenseValid() {
        if (ensembleMissense) {
            if (polyphenHumdiv.equals(Data.NO_FILTER_STR)
                    || RevelCommand.minRevel == Data.NO_FILTER
                    || PrimateAICommand.minPrimateAI == Data.NO_FILTER) {
                ErrorManager.print(ensembleMissenseMsg, ErrorManager.COMMAND_PARSING);
            }
        }
    }
}
