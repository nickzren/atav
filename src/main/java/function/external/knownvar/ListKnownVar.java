package function.external.knownvar;

import function.external.knownvar.KnownVarOutput;
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
public class ListKnownVar extends AnalysisBase {

    BufferedWriter bwKnownVar = null;
    final String knownVarFilePath = CommandValue.outputPath + "knownvar.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwKnownVar = new BufferedWriter(new FileWriter(knownVarFilePath));
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
            bwKnownVar.flush();
            bwKnownVar.close();
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
        bwKnownVar.write(KnownVarOutput.title);
        bwKnownVar.newLine();

        for (String variantId : VariantManager.getIncludeVariantList()) {
            KnownVarOutput output = new KnownVarOutput(variantId);

            bwKnownVar.write(variantId + ",");
            bwKnownVar.write(output.toString());
            bwKnownVar.newLine();

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
                bwKnownVar.write(lineStr + ",");
                bwKnownVar.write(KnownVarOutput.title);
                bwKnownVar.newLine();
                continue;
            }

            if (lineStr.isEmpty()) {
                continue;
            }

            String variantId = lineStr.substring(0, lineStr.indexOf(","));

            KnownVarOutput output = new KnownVarOutput(variantId);

            bwKnownVar.write(lineStr + ",");
            bwKnownVar.write(output.toString());
            bwKnownVar.newLine();

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
        return "It is running a list KnownVar function...";
    }
}
