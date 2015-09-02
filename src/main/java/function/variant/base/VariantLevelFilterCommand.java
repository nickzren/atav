package function.variant.base;

import function.external.evs.EvsCommand;
import function.external.kaviar.KaviarCommand;
import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.checkValuesValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.getValidInteger;
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

    public static String exacPop = "global";
    public static float exacMaf = Data.NO_FILTER;
    public static float exacVqslodSnv = Data.NO_FILTER;
    public static float exacVqslodIndel = Data.NO_FILTER;
    public static float exacMeanCoverage = Data.NO_FILTER;
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
                EvsCommand.evsMafPop = option.getValue();
            } else if (option.getName().equals("--evs-maf")) {
                checkValueValid(0.5, 0, option);
                EvsCommand.evsMaf = getValidDouble(option);
            } else if (option.getName().equals("--evs-mhgf-rec")
                    || option.getName().equals("--evs-mhgf-recessive")) {
                checkValueValid(0.5, 0, option);
                EvsCommand.evsMhgf4Recessive = getValidDouble(option);
            } else if (option.getName().equals("--min-evs-all-average-coverage")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                EvsCommand.evsAllAverageCoverage = getValidInteger(option);
            } else if (option.getName().equals("--exclude-evs-qc-failed")) {
                EvsCommand.isExcludeEvsQcFailed = true;
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
            } else if (option.getName().equals("--min-exac-mean-coverage")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                exacMeanCoverage = getValidFloat(option);
            } else if (option.getName().equals("--min-c-score")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                minCscore = getValidDouble(option);
            } else if (option.getName().equals("--max-kaviar-allele-freq")) {
                checkValueValid(1, 0, option);
                KaviarCommand.maxKaviarAlleleFreq = getValidFloat(option);
            } else if (option.getName().equals("--max-kaviar-allele-count")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                KaviarCommand.maxKaviarAlleleCount = getValidInteger(option);
            }else {
                continue;
            }

            iterator.remove();
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

    public static boolean isExacMeanCoverageValid(float value) {
        if (exacMeanCoverage == Data.NO_FILTER) {
            return true;
        }

        if (value >= exacMeanCoverage) {
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
