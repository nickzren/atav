package function.external.ccr;

import function.annotation.base.AnalysisBase4AnnotatedVar;
import function.annotation.base.AnnotatedVariant;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListCCR extends AnalysisBase4AnnotatedVar {

    private BufferedWriter bwCCR = null;
    private final String ccrFilePath = CommonCommand.outputPath + "ccr.csv";

    @Override
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            CCROutput ccrOutput = new CCROutput(annotatedVar.getGeneSet(),
                    annotatedVar.getChrStr(),
                    annotatedVar.getStartPosition());

            bwCCR.write(annotatedVar.getVariantIdStr() + ",");
            bwCCR.write(annotatedVar.getGeneName() + ",");
            bwCCR.write(ccrOutput.toString());
            bwCCR.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void initOutput() {
        try {
            bwCCR = new BufferedWriter(new FileWriter(ccrFilePath));
            bwCCR.write(CCROutput.getHeader());
            bwCCR.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwCCR.flush();
            bwCCR.close();
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
    public String toString() {
        return "Start running list ccr function";
    }
}
