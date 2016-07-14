package function.genotype.var;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListVar extends AnalysisBase4CalledVar {

    BufferedWriter bwVariants = null;
    final String variantsFilePath = CommonCommand.outputPath + "variants.csv";

    @Override
    public void initOutput() {
        try {
            bwVariants = new BufferedWriter(new FileWriter(variantsFilePath));
            bwVariants.write(VarOutput.getTitle());
            bwVariants.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwVariants.flush();
            bwVariants.close();
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
            VarOutput output = new VarOutput(calledVar);
            output.countSampleGeno();
            output.calculate();

            if (output.isValid()) {
                bwVariants.write(output.toString());
                bwVariants.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running a list variant function...";
    }
}
