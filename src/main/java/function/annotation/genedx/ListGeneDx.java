package function.annotation.genedx;

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
public class ListGeneDx extends AnalysisBase4AnnotatedVar {

    BufferedWriter bwGeneDx = null;
    final String geneDxFilePath = CommonCommand.outputPath + "genedx.csv";

    @Override
    public void initOutput() {
        try {
            bwGeneDx = new BufferedWriter(new FileWriter(geneDxFilePath));
            bwGeneDx.write(GeneDxOutput.geneDxFileTitle);
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwGeneDx.flush();
            bwGeneDx.close();
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
            GeneDxOutput output = new GeneDxOutput(annotatedVar);

            doOutput(output);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(GeneDxOutput output) throws Exception {
        bwGeneDx.write(output.getGeneDxString());
    }

    @Override
    public String toString() {
        return "It is running a list gene dx function...";
    }
}
