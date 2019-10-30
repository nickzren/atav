package function.cohort.vcf;

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
public class ListVCF extends AnalysisBase4CalledVar {

    BufferedWriter bwVCF = null;
    final String vcfFilePath = CommonCommand.outputPath + "variants.vcf";

    @Override
    public void initOutput() {
        try {
            bwVCF = new BufferedWriter(new FileWriter(vcfFilePath));
            bwVCF.write(VCFOutput.getHeader());
            bwVCF.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwVCF.flush();
            bwVCF.close();
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
            VCFOutput output = new VCFOutput(calledVar);

            bwVCF.write(output.toString());
            bwVCF.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list vcf function";
    }
}
