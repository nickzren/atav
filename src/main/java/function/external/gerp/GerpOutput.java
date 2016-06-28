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
        gerpScore = GerpManager.getScore(id);
    }

    public boolean isValid() {
        return GerpCommand.isGerpScoreValid(gerpScore);
    }

    @Override
    public String toString() {
        return FormatManager.getFloat(gerpScore);
    }
}
