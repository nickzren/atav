package function.external.gerp;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class GerpOutput {
    float gerpScore;

    public static final String title
            = "Variant ID,"
            + GerpManager.getTitle();

    public GerpOutput(String id) throws Exception{        
        gerpScore = GerpManager.getScore(id);
    }

    @Override
    public String toString() {
        return FormatManager.getFloat(gerpScore);
    }
}
