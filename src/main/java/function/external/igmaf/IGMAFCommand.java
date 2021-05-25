package function.external.igmaf;

import function.external.base.VariantAFCommand;

/**
 *
 * @author nick
 */
public class IGMAFCommand extends VariantAFCommand {
    private static IGMAFCommand single_instance = null;

    public static IGMAFCommand getInstance() {
        if (single_instance == null) {
            single_instance = new IGMAFCommand();
        }

        return single_instance;
    }
}
