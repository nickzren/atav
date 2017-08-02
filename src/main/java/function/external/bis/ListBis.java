package function.external.bis;

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
public class ListBis extends AnalysisBase4AnnotatedVar {

    private BufferedWriter bwBis = null;
    private final String bisFilePath = CommonCommand.outputPath + "bis.csv";

    @Override
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            BisOutput bisOutput = new BisOutput(annotatedVar.getGeneName(),
                    annotatedVar.getChrStr(),
                    annotatedVar.getStartPosition());

            bwBis.write(annotatedVar.getVariantIdStr() + ",");
            bwBis.write(annotatedVar.getGeneName() + ",");
            bwBis.write(bisOutput.toString());
            bwBis.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void initOutput() {
        try {
            bwBis = new BufferedWriter(new FileWriter(bisFilePath));
            bwBis.write(BisOutput.getTitle());
            bwBis.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwBis.flush();
            bwBis.close();
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
        return "Start running list bis function";
    }
}
