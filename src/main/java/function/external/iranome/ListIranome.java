package function.external.iranome;

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
public class ListIranome extends AnalysisBase {

    BufferedWriter bwIranome = null;
    final String iranomeFilePath = CommonCommand.outputPath + "iranome.csv";

    @Override
    public void initOutput() {
        try {
            bwIranome = new BufferedWriter(new FileWriter(iranomeFilePath));
            bwIranome.write(IranomeManager.getHeader());
            bwIranome.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwIranome.flush();
            bwIranome.close();
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
                float af = IranomeManager.getAF(variantId);

                if (IranomeCommand.isAFValid(af)) {
                    bwIranome.write(variantId + ",");
                    bwIranome.write(FormatManager.getFloat(af));
                    bwIranome.newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list iranome function";
    }
}
