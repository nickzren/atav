package function.external.gnomad;

/**
 *
 * @author nick
 */
public class GnomADGenomeCommand extends GnomADCommand {
    private static GnomADGenomeCommand single_instance = null;

    public static GnomADGenomeCommand getInstance() {
        if (single_instance == null) {
            single_instance = new GnomADGenomeCommand();
        }

        return single_instance;
    }
}
