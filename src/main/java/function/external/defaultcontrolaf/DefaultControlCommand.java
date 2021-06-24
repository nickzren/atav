package function.external.defaultcontrolaf;

import function.external.base.VariantAFCommand;

/**
 *
 * @author nick
 */
public class DefaultControlCommand extends VariantAFCommand {
    private static DefaultControlCommand single_instance = null;

    public static DefaultControlCommand getInstance() {
        if (single_instance == null) {
            single_instance = new DefaultControlCommand();
        }

        return single_instance;
    }
}
