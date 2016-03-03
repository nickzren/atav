package function.external.rvis;

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
public class ListRvis extends AnalysisBase4AnnotatedVar {

    private BufferedWriter bwRvis = null;
    private final String rvisFilePath = CommonCommand.outputPath + "rvis.csv";

    @Override
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            bwRvis.write(annotatedVar.variantIdStr + ",");
            bwRvis.write(annotatedVar.getGeneName() + ",");
            bwRvis.write(RvisManager.getLine(annotatedVar.getGeneName()));
            bwRvis.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void initOutput() {
        try {
            bwRvis = new BufferedWriter(new FileWriter(rvisFilePath));
            bwRvis.write("Variant ID,Gene," + RvisManager.getTitle());
            bwRvis.newLine();
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
            bwRvis.flush();
            bwRvis.close();
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
        return "It is running a list rvis function...";
    }
}
