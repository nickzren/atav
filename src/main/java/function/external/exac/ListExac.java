package function.external.exac;

import function.AnalysisBase;
import function.variant.base.VariantManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ListExac extends AnalysisBase {

    BufferedWriter bwExac = null;
    final String exacFilePath = CommonCommand.outputPath + "exac.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwExac = new BufferedWriter(new FileWriter(exacFilePath));
            bwExac.write(ExacOutput.getTitle());
            bwExac.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwExac.flush();
            bwExac.close();
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
                ExacOutput output = new ExacOutput(variantId);

                if (output.isValid()) {
                    bwExac.write(variantId + ",");
                    bwExac.write(output.toString());
                    bwExac.newLine();
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
        return "Start running list exac function...\n\n"
                + "coverage table: " + ExacManager.coverageTable + "\n\n"
                + "snv table: " + ExacManager.snvTable + "\n\n"
                + "indel table: " + ExacManager.indelTable;
    }
}
