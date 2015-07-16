package function.external.knownvar;

import function.annotation.base.AnalysisBase4AnnotatedVar;
import function.annotation.base.AnnotatedVariant;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ListKnownVar extends AnalysisBase4AnnotatedVar {

    BufferedWriter bwClinvar = null;
    final String clinvarFilePath = CommonCommand.outputPath + "clinvar.csv";

    BufferedWriter bwHGMD = null;
    final String hgmdFilePath = CommonCommand.outputPath + "hgmd.csv";

    BufferedWriter bwOMIM = null;
    final String omimFilePath = CommonCommand.outputPath + "omim.csv";

    @Override
    public void initOutput() {
        try {
            bwClinvar = new BufferedWriter(new FileWriter(clinvarFilePath));
            bwClinvar.write(ClinvarOutput.title);
            bwClinvar.newLine();

            bwHGMD = new BufferedWriter(new FileWriter(hgmdFilePath));
            bwHGMD.write(HGMDOutput.title);
            bwHGMD.newLine();

            bwOMIM = new BufferedWriter(new FileWriter(omimFilePath));
            bwOMIM.write(OMIMOutput.title);
            bwOMIM.newLine();
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
            bwOMIM.flush();
            bwOMIM.close();
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
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            ClinvarOutput clinvarOutput = new ClinvarOutput(annotatedVar.variantIdStr);
            bwClinvar.write(clinvarOutput.toString());
            bwClinvar.newLine();

            HGMDOutput hgmdOutput = new HGMDOutput(annotatedVar.variantIdStr);
            bwHGMD.write(hgmdOutput.toString());
            bwHGMD.newLine();

            OMIMOutput omimOutput = new OMIMOutput(annotatedVar);
            bwOMIM.write(omimOutput.toString());
            bwOMIM.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running a list KnownVar function... \n\n"
                + "clinvar table: " + ClinvarOutput.table + "\n\n"
                + "hgmd table: " + HGMDOutput.table + "\n\n"
                + "omim table: " + OMIMOutput.table;
    }
}
