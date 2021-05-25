package function.external.defaultcontrolaf;

import function.external.base.VariantAFCommand;

/**
 *
 * @author nick
 */
public class DefaultControlAFCommand extends VariantAFCommand {
    private static DefaultControlAFCommand single_instance = null;

    public static DefaultControlAFCommand getInstance() {
        if (single_instance == null) {
            single_instance = new DefaultControlAFCommand();
        }

        return single_instance;
    }
}
