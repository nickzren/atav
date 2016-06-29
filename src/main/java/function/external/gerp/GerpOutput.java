package function.external.gerp;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class GerpOutput {

    float gerpScore;

    public static String getTitle() {
        return "Variant ID,"
                + GerpManager.getTitle();
    }

    public GerpOutput(String id) throws Exception {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        gerpScore = GerpManager.getScore(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public boolean isValid() {
        return GerpCommand.isGerpScoreValid(gerpScore);
    }

    @Override
    public String toString() {
        return FormatManager.getFloat(gerpScore);
    }
}
