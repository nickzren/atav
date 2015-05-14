package atav.analysis.varanno;

import atav.analysis.base.AnalysisBase4AnnotatedVar;
import atav.analysis.base.AnnotatedVariant;
import atav.manager.data.GeneManager;
import atav.manager.utils.CommandValue;
import atav.manager.utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ListVarAnno extends AnalysisBase4AnnotatedVar {

    BufferedWriter bwAnnotations = null;
    final String annotationsFilePath = CommandValue.outputPath + "annotations.csv";

    @Override
    public void initOutput() {
        try {
            bwAnnotations = new BufferedWriter(new FileWriter(annotationsFilePath));
            bwAnnotations.write(VarAnnoOutput.annotationFileTitle);
            bwAnnotations.newLine();
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
            bwAnnotations.flush();
            bwAnnotations.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        GeneManager.initGeneStableIdNmNpMap();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            VarAnnoOutput output = new VarAnnoOutput(annotatedVar);

            bwAnnotations.write(output.toString());
            bwAnnotations.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running a list variant annotation function...";
    }
}
