package function.cohort.parental;

import function.cohort.base.AnalysisBase4CalledVar;
import function.cohort.base.CalledVariant;
import function.cohort.base.Sample;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author nick, quanli
 */
public class ParentalMosaic extends AnalysisBase4CalledVar {

    BufferedWriter bwOutput = null;

    final String outputFilePath = CommonCommand.outputPath + "parental.mosaic.csv";

    @Override
    public void initOutput() {
        try {
            bwOutput = new BufferedWriter(new FileWriter(outputFilePath));
            bwOutput.write(ParentalOutput.getTitle());
            bwOutput.newLine();
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
            bwOutput.flush();
            bwOutput.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        FamilyManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            ParentalOutput output = new ParentalOutput(calledVar);

            for (Family family : FamilyManager.getList()) {

                for (Sample child : family.getChildList()) {

                    if (output.isChildValid(child)) {

                        for (Sample parent : family.getParentList()) {

                            if (output.isParentValid(parent)) {
                                doOutput(output.getString());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(String str) throws IOException {
        bwOutput.write(str);
        bwOutput.newLine();
    }

    @Override
    public String toString() {
        return "Start running parental mosaic function";
    }
}
