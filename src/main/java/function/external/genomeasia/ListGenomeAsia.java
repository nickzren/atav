package function.external.genomeasia;

import function.AnalysisBase;
import function.variant.base.VariantManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ListGenomeAsia extends AnalysisBase {

    BufferedWriter bwGenomeAsia = null;
    final String genomeasiaFilePath = CommonCommand.outputPath + "genomeasia.csv";

    @Override
    public void initOutput() {
        try {
            bwGenomeAsia = new BufferedWriter(new FileWriter(genomeasiaFilePath));
            bwGenomeAsia.write(GenomeAsiaManager.getHeader());
            bwGenomeAsia.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwGenomeAsia.flush();
            bwGenomeAsia.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {
        try {
            for (String variantId : VariantManager.getIncludeVariantSet()) {
                float af = GenomeAsiaManager.getAF(variantId);

                if (GenomeAsiaCommand.getInstance().isAFValid(af, false)) {
                    bwGenomeAsia.write(variantId + ",");
                    bwGenomeAsia.write(FormatManager.getFloat(af));
                    bwGenomeAsia.newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list genomeasia function";
    }
}
