package function.external.topmed;

import function.external.base.VariantAFCommand;

/**
 *
 * @author nick
 */
public class TopMedCommand  extends VariantAFCommand {
    private static TopMedCommand single_instance = null;

    public static TopMedCommand getInstance() {
        if (single_instance == null) {
            single_instance = new TopMedCommand();
        }

        return single_instance;
    }
}
