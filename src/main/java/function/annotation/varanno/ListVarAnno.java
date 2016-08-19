package function.annotation.varanno;

import function.annotation.base.GeneManager;
import function.annotation.base.AnnotatedVariant;
import function.annotation.base.AnalysisBase4AnnotatedVar;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ListVarAnno extends AnalysisBase4AnnotatedVar {

    BufferedWriter bwAnnotations = null;
    final String annotationsFilePath = CommonCommand.outputPath + "annotations.csv";

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
        return "Start running list variant annotation function...";
    }
}
