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
            bwKnownVar.write(KnownVarOutput.getHeader());
            bwKnownVar.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
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
            knownVarOutput.init(annotatedVar.getGeneName());
            bwKnownVar.write(annotatedVar.getVariantIdStr() + ",");
            bwKnownVar.write(annotatedVar.getGeneName() + ",");
            bwKnownVar.write(knownVarOutput.toString());
            bwKnownVar.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list KnownVar function";
    }
}
