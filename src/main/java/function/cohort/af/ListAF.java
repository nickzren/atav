package function.cohort.af;

import function.cohort.base.AnalysisBase4CalledVar;
import function.cohort.base.CalledVariant;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListAF extends AnalysisBase4CalledVar {

    BufferedWriter bwAF = null;
    final String afFilePath = CommonCommand.outputPath + "af.tsv";

    @Override
    public void initOutput() {
        try {
            bwAF = new BufferedWriter(new FileWriter(afFilePath));
            bwAF.write(AFOutput.getHeader());
            bwAF.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwAF.flush();
            bwAF.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
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
    public void processVariant(CalledVariant calledVar) {
        try {
            AFOutput output = new AFOutput(calledVar);

            bwAF.write(output.toString());
            bwAF.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list af function";
    }
}
