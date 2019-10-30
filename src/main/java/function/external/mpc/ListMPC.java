package function.external.mpc;

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
public class ListMPC extends AnalysisBase {

    BufferedWriter bwMPC = null;
    final String gerpFilePath = CommonCommand.outputPath + "mpc.csv";

    @Override
    public void initOutput() {
        try {
            bwMPC = new BufferedWriter(new FileWriter(gerpFilePath));
            bwMPC.write(MPCOutput.getHeader());
            bwMPC.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwMPC.flush();
            bwMPC.close();
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
                MPCOutput output = new MPCOutput(variantId);

                if (output.isValid()) {
                    bwMPC.write(variantId + ",");
                    bwMPC.write(output.toString());
                    bwMPC.newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list mpc function";
    }

}
