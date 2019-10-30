package function.external.gerp;

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
public class ListGerp extends AnalysisBase {

    BufferedWriter bwGerp = null;
    final String gerpFilePath = CommonCommand.outputPath + "gerp.csv";

    @Override
    public void initOutput() {
        try {
            bwGerp = new BufferedWriter(new FileWriter(gerpFilePath));
            bwGerp.write(GerpOutput.getHeader());
            bwGerp.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwGerp.flush();
            bwGerp.close();
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
                GerpOutput output = new GerpOutput(variantId);

                if (output.isValid()) {
                    bwGerp.write(variantId + ",");
                    bwGerp.write(output.toString());
                    bwGerp.newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    @Override
    public String toString() {
        return "Start running list gerp function";
    }
}
