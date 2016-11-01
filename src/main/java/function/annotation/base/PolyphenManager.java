package function.annotation.base;

import global.Data;

/**
 *
 * @author nick
 */
public class PolyphenManager {

    public static String getPrediction(float score, String effect) {        
        if (score == Data.NA) {
            if (effect.equals("missense_variant")
                    || effect.equals("splice_region_variant")) {
                return "unknown";
            } else {
                return "NA";
            }
        }

        if (score < 0.4335) { //based on Liz's comment
            return "benign";
        }

        if (score < 0.9035) { //based on Liz's comment
            return "possibly";
        }

        return "probably";
    }

    public static boolean isValid(float score, String effect, String inputPrediction) {
        String prediction = getPrediction(score, effect);

        if (effect.equals("missense_variant")
                || effect.equals("splice_region_variant")) {
            return inputPrediction.contains(prediction);
        } else {
            return true;
        }
    }
}
