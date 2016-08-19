package function.external.kaviar;

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
public class ListKaviar extends AnalysisBase {

    BufferedWriter bwKaviar = null;
    final String kaviarFilePath = CommonCommand.outputPath + "kaviar.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwKaviar = new BufferedWriter(new FileWriter(kaviarFilePath));
            bwKaviar.write(KaviarOutput.getTitle());
            bwKaviar.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }
    
    @Override
    public void closeOutput() {
        try {
            bwKaviar.flush();
            bwKaviar.close();
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
                KaviarOutput output = new KaviarOutput(variantId);

                if (output.isValid()) {
                    bwKaviar.write(variantId + ",");
                    bwKaviar.write(output.toString());
                    bwKaviar.newLine();
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
        return "Start running list kaviar function...\n\n"
                + "snv table: " + KaviarManager.snvTable + "\n\n"
                + "indel table: " + KaviarManager.indelTable;
    }
}
