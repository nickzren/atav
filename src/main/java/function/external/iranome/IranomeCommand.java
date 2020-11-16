package function.external.iranome;

import function.external.base.VariantAFCommand;

/**
 *
 * @author nick
 */
public class IranomeCommand extends VariantAFCommand {
    private static IranomeCommand single_instance = null;

    public static IranomeCommand getInstance() {
        if (single_instance == null) {
            single_instance = new IranomeCommand();
        }

        return single_instance;
    }
}
