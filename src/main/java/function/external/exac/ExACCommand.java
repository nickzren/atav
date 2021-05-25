package function.external.exac;

import function.external.base.VariantAFCommand;
import global.Data;

/**
 *
 * @author nick
 */
public class ExACCommand extends VariantAFCommand {


    public static String pop = "global";
    public static float vqslodSnv = Data.NO_FILTER;
    public static float vqslodIndel = Data.NO_FILTER;
    public static float meanCoverage = Data.NO_FILTER;

    private static ExACCommand single_instance = null;

    public static ExACCommand getInstance() {
        if (single_instance == null) {
            single_instance = new ExACCommand();
        }

        return single_instance;
    }
    
    public static boolean isVqslodValid(float value, boolean isSnv) {
        if (isSnv) {
            return isVqslodSnvValid(value);
        } else {
            return isVqslodIndelValid(value);
        }
    }

    private static boolean isVqslodSnvValid(float value) {
        if (vqslodSnv == Data.NO_FILTER) {
            return true;
        }

        return value >= vqslodSnv
                || value == Data.FLOAT_NA;
    }

    private static boolean isVqslodIndelValid(float value) {
        if (vqslodIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= vqslodIndel
                || value == Data.FLOAT_NA;
    }

    public static boolean isMeanCoverageValid(float value) {
        if (meanCoverage == Data.NO_FILTER) {
            return true;
        }

        return value >= meanCoverage;
    }
}
