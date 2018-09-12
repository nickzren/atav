package function.external.trap;

import function.AnalysisBase;
import function.variant.base.VariantManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListTrap extends AnalysisBase {

    BufferedWriter bwTrap = null;
    final String trapFilePath = CommonCommand.outputPath + "trap.csv";

    @Override
    public void initOutput() {
        try {
            bwTrap = new BufferedWriter(new FileWriter(trapFilePath));
            bwTrap.write(TrapOutput.getTitle());
            bwTrap.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwTrap.flush();
            bwTrap.close();
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
    public void processDatabaseData() {
        try {
            for (String variantId : VariantManager.getIncludeVariantSet()) {
                TrapOutput output = new TrapOutput(variantId);
                bwTrap.write(output.toString());
                bwTrap.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list trap function";
    }
}
