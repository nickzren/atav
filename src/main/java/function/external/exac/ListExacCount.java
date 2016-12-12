package function.external.exac;

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
public class ListExacCount extends AnalysisBase4AnnotatedVar {

    private BufferedWriter bwExacCount = null;
    private final String exacCountFilePath = CommonCommand.outputPath + "exac_count.csv";

    @Override
    public void processVariant(AnnotatedVariant annotatedVar) {
        try {
            bwExacCount.write(annotatedVar.getVariantIdStr() + ",");
            bwExacCount.write("'" + annotatedVar.getGeneName() + "',");
            bwExacCount.write(ExacManager.getGeneDamagingCountsLine(annotatedVar.getGeneName()));
            bwExacCount.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void initOutput() {
        try {
            bwExacCount = new BufferedWriter(new FileWriter(exacCountFilePath));
            bwExacCount.write("Variant ID,Gene," + ExacManager.getCountByGene("title"));
            bwExacCount.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwExacCount.flush();
            bwExacCount.close();
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
        return "Start running list exac count function";
    }
}
