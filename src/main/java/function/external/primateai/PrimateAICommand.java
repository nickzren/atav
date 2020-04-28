package function.external.primateai;

import function.annotation.base.AnnotationLevelFilterCommand;
import global.Data;

/**
 *
 * @author nick
 */
public class PrimateAICommand {
    
    public static boolean isList = false;
    public static boolean isInclude = false;

    // filter option
    public static float minPrimateAI = Data.NO_FILTER;

    public static boolean isMinPrimateAIValid(float value) {
        if (value == Data.FLOAT_NA
                || minPrimateAI == Data.NO_FILTER) {
            return true;
        }

        return value >= minPrimateAI;
    }
    
    // applied at variant level when --ensemble-missense not applied
    public static boolean isValid(float primateAI, String effect) {
        if (isInclude && !AnnotationLevelFilterCommand.ensembleMissense) {
            // PrimateAI filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                return isMinPrimateAIValid(primateAI);
            } else {
                return true;
            }
        }
        
        return true;
    }
}
