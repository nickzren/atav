package function.genotype.statistics;

import global.Data;
import java.util.Iterator;
import static utils.CommandManager.checkValueValid;
import static utils.CommandManager.getValidDouble;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class StatisticsCommand {

    public static boolean isFisher = false;
    public static boolean isLinear = false;
    public static double threshold4Sort = Data.NO_FILTER;
    public static String[] models = {"allelic", "dominant", "recessive", "genotypic"};
    public static boolean isCaseOnly = false;
    public static String covariateFile = "";
    public static String quantitativeFile = "";
    public static int minHomCaseRec = Data.NO_FILTER; // collasping, fisher, linear

    public static void initFisherOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--threshold-sort")) {
                threshold4Sort = getValidDouble(option);
                checkValueValid(1, 0, option);
            } else if (option.getName().equals("--case-only")) {
                isCaseOnly = true;
            } else if (option.getName().equals("--min-hom-case-rec")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                minHomCaseRec = getValidInteger(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    public static void initLinearOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--threshold-sort")) {
                threshold4Sort = getValidDouble(option);
                checkValueValid(1, 0, option);
            } else if (option.getName().equals("--covariate")) {
                covariateFile = getValidPath(option);
            } else if (option.getName().equals("--quantitative")) {
                quantitativeFile = getValidPath(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    public static boolean isMinHomCaseRecValid(int value) {
        if (minHomCaseRec == Data.NO_FILTER) {
            return true;
        }

        if (value >= minHomCaseRec) {
            return true;
        }

        return false;
    }
}
