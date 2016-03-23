package function.variant.base;

import function.external.evs.EvsCommand;
import function.external.evs.EvsManager;
import function.external.exac.ExacCommand;
import function.external.exac.ExacManager;
import function.external.gerp.GerpCommand;
import function.external.kaviar.KaviarCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.rvis.RvisCommand;
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
                checkValuesValid(EvsManager.EVS_POP, option);
                EvsCommand.evsPop = option.getValue();
                EvsCommand.isIncludeEvs = true;
            } else if (option.getName().equals("--evs-maf")) {
                checkValueValid(0.5, 0, option);
                EvsCommand.evsMaf = getValidDouble(option);
                EvsCommand.isIncludeEvs = true;
            } else if (option.getName().equals("--min-evs-all-average-coverage")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                EvsCommand.evsAllAverageCoverage = getValidInteger(option);
                EvsCommand.isIncludeEvs = true;
            } else if (option.getName().equals("--exclude-evs-qc-failed")) {
                EvsCommand.isExcludeEvsQcFailed = true;
                EvsCommand.isIncludeEvs = true;
            } else if (option.getName().equals("--exac-pop")) {
                checkValuesValid(ExacManager.EXAC_POP, option);
                ExacCommand.exacPop = option.getValue();
                ExacCommand.isIncludeExac = true;
            } else if (option.getName().equals("--exac-maf")) {
                checkValueValid(0.5, 0, option);
                ExacCommand.exacMaf = getValidFloat(option);
                ExacCommand.isIncludeExac = true;
            } else if (option.getName().equals("--min-exac-vqslod-snv")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                ExacCommand.exacVqslodSnv = getValidFloat(option);
                ExacCommand.isIncludeExac = true;
            } else if (option.getName().equals("--min-exac-vqslod-indel")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                ExacCommand.exacVqslodIndel = getValidFloat(option);
                ExacCommand.isIncludeExac = true;
            } else if (option.getName().equals("--min-exac-mean-coverage")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                ExacCommand.exacMeanCoverage = getValidFloat(option);
                ExacCommand.isIncludeExac = true;
            } else if (option.getName().equals("--exac-subset")) {
                checkValuesValid(ExacManager.EXAC_SUBSET, option);
                ExacCommand.exacSubset = option.getValue();
                ExacManager.resetTables();
                ExacCommand.isIncludeExac = true;
            } else if (option.getName().equals("--min-c-score")
                    || option.getName().equals("--min-cadd-score")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                minCscore = getValidDouble(option);
            } else if (option.getName().equals("--max-kaviar-maf")) {
                checkValueValid(1, 0, option);
                KaviarCommand.maxKaviarMaf = getValidFloat(option);
                KaviarCommand.isIncludeKaviar = true;
            } else if (option.getName().equals("--max-kaviar-allele-count")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                KaviarCommand.maxKaviarAlleleCount = getValidInteger(option);
                KaviarCommand.isIncludeKaviar = true;
            } else if (option.getName().equals("--include-evs")) {
                EvsCommand.isIncludeEvs = true;
            } else if (option.getName().equals("--include-exac")) {
                ExacCommand.isIncludeExac = true;
            } else if (option.getName().equals("--include-gerp")) {
                GerpCommand.isIncludeGerp = true;
            } else if (option.getName().equals("--include-kaviar")) {
                KaviarCommand.isIncludeKaviar = true;
            } else if (option.getName().equals("--include-known-var")) {
                KnownVarCommand.isIncludeKnownVar = true;
            } else if (option.getName().equals("--include-rvis")) {
                RvisCommand.isIncludeRvis = true;
            } else if (option.getName().equals("--min-gerp-score")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                GerpCommand.minGerpScore = getValidFloat(option);
                GerpCommand.isIncludeGerp = true;
            } else {
                continue;
            }

            iterator.remove();
        }
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
