package function.annotation.varanno;

import function.annotation.base.AnnotatedVariant;
import function.annotation.base.AnalysisBase4AnnotatedVar;
import utils.CommandValue;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ListNewVarId extends AnalysisBase4AnnotatedVar {

    BufferedWriter bwVarId = null;
    final String newVarIdFilePath = CommandValue.outputPath + "new.var.id.txt";

    @Override
    public void initOutput() {
        try {
            bwVarId = new BufferedWriter(new FileWriter(newVarIdFilePath));
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
            bwVarId.flush();
            bwVarId.close();
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
            bwVarId.write(annotatedVar.getVariantIdStr());
            bwVarId.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running a list new variant id function...";
    }
}
