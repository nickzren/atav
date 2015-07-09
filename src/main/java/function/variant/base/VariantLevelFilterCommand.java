package function.variant.base;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;
import utils.CommonCommand;

/**
 *
 * @author nick
 */
public class VariantLevelFilterCommand {

    // Variant Level Filter Options
    public static String includeVariantId = "";
    public static String excludeVariantId = "";
    public static boolean isExcludeArtifacts = false;
    public static boolean isExcludeSnv = false;
    public static boolean isExcludeIndel = false;
    public static String evsMafPop = "all";
    public static double evsMaf = Data.NO_FILTER;
    public static double evsMhgf4Recessive = Data.NO_FILTER;
    public static boolean isOldEvsUsed = false;
    public static boolean isExcludeEvsQcFailed = false;
    public static String exacPop = "global";
    public static float exacMaf = Data.NO_FILTER;
    public static float exacVqslodSnv = Data.NO_FILTER;
    public static float exacVqslodIndel = Data.NO_FILTER;
    public static double minCscore = Data.NO_FILTER;

    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--region")) {
                CommonCommand.regionInput = option.getValue();
            } else if (option.getName().equals("--variant")) {
                includeVariantId = getValidPath(option);
            } else if (option.getName().equals("--exclude-variant")) {
                excludeVariantId = getValidPath(option);
            } else if (option.getName().equals("--exclude-artifacts")) {
                isExcludeArtifacts = true;
            } else if (option.getName().equals("--exclude-snv")) {
                isExcludeSnv = true;
            } else if (option.getName().equals("--exclude-indel")) {
                isExcludeIndel = true;
            } else if (option.getName().equals("--evs-pop")
                    || option.getName().equals("--evs-maf-pop")) {
                checkValuesValid(Data.EVS_POP, option);
                evsMafPop = option.getValue();
            } else if (option.getName().equals("--evs-maf")) {
                checkValueValid(0.5, 0, option);
                evsMaf = getValidDouble(option);
                isOldEvsUsed = true;
            } else if (option.getName().equals("--evs-mhgf-rec")
                    || option.getName().equals("--evs-mhgf-recessive")) {
                checkValueValid(0.5, 0, option);
                evsMhgf4Recessive = getValidDouble(option);
                isOldEvsUsed = true;
            } else if (option.getName().equals("--exclude-evs-qc-failed")) {
                isExcludeEvsQcFailed = true;
                isOldEvsUsed = true;
            } else if (option.getName().equals("--exac-pop")) {
                checkValuesValid(Data.EXAC_POP, option);
                exacPop = option.getValue();
            } else if (option.getName().equals("--exac-maf")) {
                checkValueValid(0.5, 0, option);
                exacMaf = getValidFloat(option);
            } else if (option.getName().equals("--min-exac-vqslod-snv")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                exacVqslodSnv = getValidFloat(option);
            } else if (option.getName().equals("--min-exac-vqslod-indel")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                exacVqslodIndel = getValidFloat(option);
            } else if (option.getName().equals("--min-c-score")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                minCscore = getValidDouble(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    public static boolean isEvsMafValid(double value) {
        if (evsMaf == Data.NO_FILTER) {
            return true;
        }

        if (value <= evsMaf) {
            return true;
        }

        return false;
    }

    public static boolean isEvsMhgf4RecessiveValid(double value) {
        if (evsMhgf4Recessive == Data.NO_FILTER) {
            return true;
        }

        if (value <= evsMhgf4Recessive) {
            return true;
        }

        return false;
    }

    public static boolean isEvsStatusValid(String status) {
        if (isExcludeEvsQcFailed) {
            if (status.equalsIgnoreCase("NA")
                    || status.equalsIgnoreCase("pass")) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean isExacMafValid(float value) {
        if (exacMaf == Data.NO_FILTER) {
            return true;
        }

        if (value <= exacMaf) {
            return true;
        }

        return false;
    }

    public static boolean isExacVqslodValid(float value, boolean isSnv) {
        if (isSnv) {
            return isExacVqslodSnvValid(value);
        } else {
            return isExacVqslodIndelValid(value);
        }
    }

    private static boolean isExacVqslodSnvValid(float value) {
        if (exacVqslodSnv == Data.NO_FILTER) {
            return true;
        }

        if (value >= exacVqslodSnv
                || value == Data.NA) {
            return true;
        }

        return false;
    }

    private static boolean isExacVqslodIndelValid(float value) {
        if (exacVqslodIndel == Data.NO_FILTER) {
            return true;
        }

        if (value >= exacVqslodIndel
                || value == Data.NA) {
            return true;
        }

        return false;
    }

    public static boolean isCscoreValid(float value) {
        if (value == Data.NA
                || minCscore == Data.NO_FILTER) {
            return true;
        }

        if (value >= minCscore) {
            return true;
        }

        return false;
    }
}
