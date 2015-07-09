package function.external.knownvar;

import function.AnalysisBase;
import function.variant.base.VariantManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author nick
 */
public class ListKnownVar extends AnalysisBase {

    BufferedWriter bwClinvar = null;
    final String clinvarFilePath = CommonCommand.outputPath + "clinvar.csv";

    BufferedWriter bwHGMD = null;
    final String hgmdFilePath = CommonCommand.outputPath + "hgmd.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwClinvar = new BufferedWriter(new FileWriter(clinvarFilePath));
            bwClinvar.write(ClinvarOutput.title);
            bwClinvar.newLine();

            bwHGMD = new BufferedWriter(new FileWriter(hgmdFilePath));
            bwHGMD.write(HGMDOutput.title);
            bwHGMD.newLine();
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
            bwClinvar.flush();
            bwClinvar.close();
            bwHGMD.flush();
            bwHGMD.close();
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
                doOutput(bwClinvar, new ClinvarOutput(variantId));

                doOutput(bwHGMD, new HGMDOutput(variantId));

                countVariant();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(BufferedWriter bw, Output output) throws IOException {
        bw.write(output.toString());
        bw.newLine();
    }

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant "
                + analyzedRecords + "                     \r");
    }

    @Override
    public String toString() {
        return "It is running a list KnownVar function... \n\n"
                + "clinvar table: " + ClinvarOutput.table + "\n\n"
                + "hgmd table: " + HGMDOutput.table;
    }
}
