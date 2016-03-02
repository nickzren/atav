package function.external.subrvis;

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
public class ListSubRvis extends AnalysisBase4AnnotatedVar {

    private BufferedWriter bwSubRvis = null;
    private final String subRvisFilePath = CommonCommand.outputPath + "subrvis.csv";

    @Override
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            SubRvisOutput subRvisOutput = new SubRvisOutput(annotatedVar.getGeneName(),
                    annotatedVar.getRegion().getChrStr(),
                    annotatedVar.getRegion().getStartPosition());

            bwSubRvis.write(annotatedVar.variantIdStr + ",");
            bwSubRvis.write(annotatedVar.getGeneName() + ",");
            bwSubRvis.write(subRvisOutput.toString());
            bwSubRvis.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void initOutput() {
        try {
            bwSubRvis = new BufferedWriter(new FileWriter(subRvisFilePath));
            bwSubRvis.write(SubRvisOutput.title);
            bwSubRvis.newLine();
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
            bwSubRvis.flush();
            bwSubRvis.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        SubRvisManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public String toString() {
        return "It is running a list sub rvis function...";
    }
}
