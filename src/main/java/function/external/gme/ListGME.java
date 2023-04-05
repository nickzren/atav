package function.external.gme;

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
public class ListGME extends AnalysisBase {

    BufferedWriter bwGME = null;
    final String gmeFilePath = CommonCommand.outputPath + "gme.csv";

    @Override
    public void initOutput() {
        try {
            bwGME = new BufferedWriter(new FileWriter(gmeFilePath));
            bwGME.write(GMEManager.getHeader());
            bwGME.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwGME.flush();
            bwGME.close();
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
                float af = GMEManager.getAF(variantId);

                if (GMECommand.getInstance().isAFValid(af, null)) {
                    bwGME.write(variantId + ",");
                    bwGME.write(FormatManager.getFloat(af));
                    bwGME.newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list gme function";
    }
}
