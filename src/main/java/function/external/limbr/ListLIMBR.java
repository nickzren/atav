package function.external.limbr;

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
public class ListLIMBR extends AnalysisBase4AnnotatedVar {

    private BufferedWriter bwLIMBR = null;
    private final String limbrFilePath = CommonCommand.outputPath + "limbr.csv";

    @Override
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            LIMBROutput limbrOutput = new LIMBROutput(annotatedVar.getGeneName(),
                    annotatedVar.getChrStr(),
                    annotatedVar.getStartPosition());

            bwLIMBR.write(annotatedVar.getVariantIdStr() + ",");
            bwLIMBR.write(annotatedVar.getGeneName() + ",");
            bwLIMBR.write(limbrOutput.toString());
            bwLIMBR.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void initOutput() {
        try {
            bwLIMBR = new BufferedWriter(new FileWriter(limbrFilePath));
            bwLIMBR.write(LIMBROutput.getHeader());
            bwLIMBR.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwLIMBR.flush();
            bwLIMBR.close();
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
        return "Start running list limbr function";
    }
}
