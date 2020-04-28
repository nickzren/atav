package function.external.revel;

import function.annotation.base.AnnotationLevelFilterCommand;
import global.Data;

/**
 *
 * @author nick
 */
public class RevelCommand {

    public static boolean isList = false;
    public static boolean isInclude = false;

    // filter option
    public static float minRevel = Data.NO_FILTER;

    public static boolean isMinRevelValid(float value) {
        if (value == Data.FLOAT_NA
                || minRevel == Data.NO_FILTER) {
            return true;
        }

        return value >= minRevel;
    }
    
    // applied at variant level when --ensemble-missense not applied
    public static boolean isValid(float revel, String effect) {
        if (isInclude && !AnnotationLevelFilterCommand.ensembleMissense) {
            // REVEL filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                return isMinRevelValid(revel);
            } else {
                return true;
            }
        }

        return true;
    }
}
