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

    BufferedWriter bwKnownVar = null;
    final String knownVarFilePath = CommonCommand.outputPath + "knownvar.csv";

    @Override
    public void initOutput() {
        try {
            bwKnownVar = new BufferedWriter(new FileWriter(knownVarFilePath));
            bwKnownVar.write(KnownVarOutput.title);
            bwKnownVar.newLine();
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
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            KnownVarOutput knownVarOutput = new KnownVarOutput(annotatedVar);

            bwKnownVar.write(annotatedVar.variantIdStr + ",");
            bwKnownVar.write(annotatedVar.getGeneName() + ",");
            bwKnownVar.write(knownVarOutput.toString());
            bwKnownVar.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running a list KnownVar function... \n\n"
                + "KnownVar database tables: \n\n"
                + KnownVarManager.clinVarTable + "\n\n"
                + KnownVarManager.clinVarPathoratioTable + "\n\n"
                + KnownVarManager.hgmdTable + "\n\n"
                + KnownVarManager.omimTable + "\n\n"
                + KnownVarManager.acmgTable + "\n\n"
                + KnownVarManager.adultOnsetTable + "\n\n"
                + KnownVarManager.clinGenTable + "\n\n"
                + KnownVarManager.pgxTable + "\n\n"
                + KnownVarManager.recessiveCarrierTable;
    }
}
