package function.external.evs;

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
public class ListEvs extends AnalysisBase {

    BufferedWriter bwEvs = null;
    final String evsFilePath = CommonCommand.outputPath + "evs.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwEvs = new BufferedWriter(new FileWriter(evsFilePath));
            bwEvs.write(EvsOutput.title);
            bwEvs.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwEvs.flush();
            bwEvs.close();
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
                EvsOutput output = new EvsOutput(variantId);

                if (output.isValid()) {
                    bwEvs.write(variantId + ",");
                    bwEvs.write(output.toString());
                    bwEvs.newLine();
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
        return "It is running list evs function...\n\n"
                + "coverage table: " + EvsManager.coverageTable + "\n\n"
                + "snv table: " + EvsManager.snvTable + "\n\n"
                + "indel table: " + EvsManager.indelTable;
    }
}
