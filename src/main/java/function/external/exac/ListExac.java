package function.external.exac;

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
public class ListExac extends AnalysisBase {

    BufferedWriter bwExac = null;
    final String exacFilePath = CommandValue.outputPath + "exac.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwExac = new BufferedWriter(new FileWriter(exacFilePath));
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
        bwExac.write(ExacOutput.title);
        bwExac.newLine();

        for (String variantId : VariantManager.getIncludeVariantList()) {
            ExacOutput output = new ExacOutput(variantId);

            bwExac.write(variantId + ",");
            bwExac.write(output.toString());
            bwExac.newLine();

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
                bwExac.write(lineStr + ",");
                bwExac.write(ExacOutput.title);
                bwExac.newLine();
                continue;
            }

            if (lineStr.isEmpty()) {
                continue;
            }

            String variantId = lineStr.substring(0, lineStr.indexOf(","));

            ExacOutput output = new ExacOutput(variantId);

            bwExac.write(lineStr + ",");
            bwExac.write(output.toString());
            bwExac.newLine();

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
        return "It is running a list exac function...";
    }
}
