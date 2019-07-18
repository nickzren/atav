package function.external.pext;

import function.AnalysisBase;
import function.variant.base.VariantManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListPext extends AnalysisBase {

    BufferedWriter bwPext = null;
    final String pextFilePath = CommonCommand.outputPath + "pext.csv";

    @Override
    public void initOutput() {
        try {
            bwPext = new BufferedWriter(new FileWriter(pextFilePath));
            bwPext.write(PextOutput.getTitle());
            bwPext.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwPext.flush();
            bwPext.close();
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
                PextOutput output = new PextOutput(variantId);
                bwPext.write(output.toString());
                bwPext.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list pext function";
    }
}
