package function.external.genomeasia;

import function.external.base.VariantAFCommand;

/**
 *
 * @author nick
 */
public class GenomeAsiaCommand extends VariantAFCommand {
    private static GenomeAsiaCommand single_instance = null;

    public static GenomeAsiaCommand getInstance() {
        if (single_instance == null) {
            single_instance = new GenomeAsiaCommand();
        }

        return single_instance;
    }
}
