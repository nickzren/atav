package function.external.evs;

import function.base.AnalysisBase;
import function.variant.base.VariantManager;
import utils.CommandValue;
import utils.ErrorManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 *
 * @author nick
 */
public class ListEvs extends AnalysisBase {

    BufferedWriter bwEvs = null;
    final String evsFilePath = CommandValue.outputPath + "evs.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwEvs = new BufferedWriter(new FileWriter(evsFilePath));
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
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
            if (!VariantManager.getIncludeVariantList().isEmpty()) {
                listByVariantList();
            } else if (!CommandValue.variantInputFile.isEmpty()) {
                listByVariantInputFile();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void listByVariantList() throws Exception {
        bwEvs.write(EvsOutput.title);
        bwEvs.newLine();

        for (String variantId : VariantManager.getIncludeVariantList()) {
            EvsOutput output = new EvsOutput(variantId);

            bwEvs.write(variantId + ",");
            bwEvs.write(output.toString());
            bwEvs.newLine();

            countVariant();
        }
    }

    private void listByVariantInputFile() throws Exception {
        String lineStr = "";

        File f = new File(CommandValue.variantInputFile);
        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        boolean isHeader = true;

        while ((lineStr = br.readLine()) != null) {
            if (isHeader) {
                isHeader = false;
                bwEvs.write(lineStr + ",");
                bwEvs.write(EvsOutput.title);
                bwEvs.newLine();
                continue;
            }

            if (lineStr.isEmpty()) {
                continue;
            }

            String variantId = lineStr.substring(0, lineStr.indexOf(","));

            EvsOutput output = new EvsOutput(variantId);

            bwEvs.write(lineStr + ",");
            bwEvs.write(output.toString());
            bwEvs.newLine();

            countVariant();
        }
    }

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant "
                + analyzedRecords + "                     \r");
    }

    @Override
    public String toString() {
        return "It is running a list evs function...";
    }
}
