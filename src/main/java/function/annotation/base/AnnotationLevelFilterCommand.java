package function.annotation.base;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class AnnotationLevelFilterCommand {

    public static String functionInput = "";
    public static String geneInput = "";
    public static String geneBoundaryFile = "";
    public static String transcriptFile = "";
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
            if (option.getName().equals("--function")) {
                functionInput = getValidPath(option);
            } else if (option.getName().equals("--gene")) {
                geneInput = getValidPath(option);
            } else if (option.getName().equals("--gene-boundaries") ||
                    option.getName().equals("--gene-boundary")) {
                geneBoundaryFile = getValidPath(option);
            } else if (option.getName().equals("--transcript")) {
                transcriptFile = getValidPath(option);
            } else if (option.getName().equals("--ccds-only")) {
                isCcdsOnly = true;
                TranscriptManager.initCCDSTranscriptPath();
            } else if (option.getName().equals("--canonical-only")) {
                isCanonicalOnly = true;
                TranscriptManager.initCanonicalTranscriptPath();
            } else if (option.getName().equals("--polyphen")
                    || option.getName().equals("--polyphen-humdiv")) {
                checkValuesValid(POLYPHEN_CAT, option);
                polyphenHumdiv = option.getValue();
            } else if (option.getName().equals("--polyphen-humvar")) {
                checkValuesValid(POLYPHEN_CAT, option);
                polyphenHumvar = option.getValue();
            } else {
                continue;
            }

            iterator.remove();
        }
    }
}
