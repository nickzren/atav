package function.annotation.base;

import global.Data;

/**
 *
 * @author nick
 */
public class PolyphenManager {

    public static final String[] POLYPHEN_CAT = {"probably", "possibly", "unknown", "benign"};

    public static String getPrediction(float score, String effect) {
        if (score == Data.FLOAT_NA) {
            if (effect.startsWith("missense_variant")
                    || effect.equals("splice_region_variant")) {
                return POLYPHEN_CAT[2];
            } else {
                return Data.STRING_NA;
            }
        }

        if (score < 0.4335) {
            return POLYPHEN_CAT[3];
        }

        if (score < 0.9035) {
            return POLYPHEN_CAT[1];
        }

        return POLYPHEN_CAT[0];
    }

    public static boolean isValid(float score, String effect, String inputPrediction) {
        if (inputPrediction.equals(Data.NO_FILTER_STR)) {
            return true;
        }

        String prediction = getPrediction(score, effect);

        if (effect.startsWith("missense_variant")
                || effect.equals("splice_region_variant")) {
            return inputPrediction.contains(prediction);
        } else {
            return true;
        }
    }

    // when --ensemble-missense used, always return true here
    public static boolean isValid(float polyphenHumdiv, float polyphenHumvar, String effect) {
        if (AnnotationLevelFilterCommand.ensembleMissense) {
            return true;
        }

        return isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                && isValid(polyphenHumvar, effect, AnnotationLevelFilterCommand.polyphenHumvar);
    }
}
