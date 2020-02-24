package function.external.topmed;

import function.AnalysisBase;
import function.variant.base.VariantManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ListTopMed extends AnalysisBase {

    BufferedWriter bwTopMed = null;
    final String topmedFilePath = CommonCommand.outputPath + "topmed.csv";

    @Override
    public void initOutput() {
        try {
            bwTopMed = new BufferedWriter(new FileWriter(topmedFilePath));
            bwTopMed.write(TopMedManager.getHeader());
            bwTopMed.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwTopMed.flush();
            bwTopMed.close();
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
                float af = TopMedManager.getAF(variantId);

                if (TopMedCommand.isMaxAFValid(af)) {
                    bwTopMed.write(variantId + ",");
                    bwTopMed.write(FormatManager.getFloat(af));
                    bwTopMed.newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list topmed function";
    }
}

