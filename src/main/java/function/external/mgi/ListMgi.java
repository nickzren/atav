package function.external.mgi;

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
public class ListMgi extends AnalysisBase4AnnotatedVar {

    private BufferedWriter bwMgi = null;
    private final String mgiFilePath = CommonCommand.outputPath + "mgi.csv";

    @Override
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            bwMgi.write(annotatedVar.getVariantIdStr() + ",");
            bwMgi.write(annotatedVar.getGeneName() + ",");
            bwMgi.write(MgiManager.getLine(annotatedVar.getGeneName()));
            bwMgi.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void initOutput() {
        try {
            bwMgi = new BufferedWriter(new FileWriter(mgiFilePath));
            bwMgi.write("Variant ID,Gene," + MgiManager.getTitle());
            bwMgi.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwMgi.flush();
            bwMgi.close();
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
        return "Start running list mgi function";
    }
}