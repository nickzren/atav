package atav.manager.data;

/**
 *
 * @author nick
 */
public class PolyphenManager {

    public static String getPrediction(double score, String function) {
        if (score < 0) {
            if (function.startsWith("NON_SYNONYMOUS")) {
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

    public static boolean isValid(double score, String function, 
            String prediction) {
        String polyphenPrediction = getPrediction(score, function);

        if (function.startsWith("NON_SYNONYMOUS")) {
            return prediction.contains(polyphenPrediction);
        } else {
            return true;
        }
    }
}
