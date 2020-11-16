package function.external.gme;

import function.external.base.VariantAFCommand;

/**
 *
 * @author nick
 */
public class GMECommand extends VariantAFCommand {
    private static GMECommand single_instance = null;

    public static GMECommand getInstance() {
        if (single_instance == null) {
            single_instance = new GMECommand();
        }

        return single_instance;
    }
}
