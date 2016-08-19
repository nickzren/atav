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

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwGerp = new BufferedWriter(new FileWriter(gerpFilePath));
            bwGerp.write(GerpOutput.getTitle());
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
            for (String variantId : VariantManager.getIncludeVariantList()) {
                GerpOutput output = new GerpOutput(variantId);

                if (output.isValid()) {
                    bwGerp.write(variantId + ",");
                    bwGerp.write(output.toString());
                    bwGerp.newLine();
                }

                countVariant();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant "
                + analyzedRecords + "                     \r");
    }

    @Override
    public String toString() {
        return "Start running list gerp function...";
    }
}
