package function.annotation.base;

import java.util.Iterator;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

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
    public static String polyphenHumdiv = "probably,possibly,unknown,benign";
    public static String polyphenHumvar = "probably,possibly,unknown,benign";

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
                default:
                    continue;
            }

            iterator.remove();
        }
    }
}
