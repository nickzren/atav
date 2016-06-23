package function.genotype.vargeno;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ListVarGeno extends AnalysisBase4CalledVar {

    BufferedWriter bwDetails = null;
    BufferedWriter bwSampleVariantCount = null;

    final String genotypesFilePath = CommonCommand.outputPath + "genotypes.csv";
    final String sampleVariantCountFilePath = CommonCommand.outputPath + "sample.variant.count.csv";

    @Override
    public void initOutput() {
        try {
            bwDetails = new BufferedWriter(new FileWriter(genotypesFilePath));
            bwDetails.write(VarGenoOutput.title);
            bwDetails.newLine();
            //bwDirty = new BufferedWriter(new FileWriter(dirtyFilePath));
            bwSampleVariantCount = new BufferedWriter(new FileWriter(sampleVariantCountFilePath));
            bwSampleVariantCount.write(SampleVariantCount.title);
            bwSampleVariantCount.newLine();
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
            bwDetails.flush();
            bwDetails.close();
            bwSampleVariantCount.flush();
            bwSampleVariantCount.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        SampleVariantCount.init();
    }

    @Override
    public void afterProcessDatabaseData() {
        outputSampleVariantCount();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            VarGenoOutput output = new VarGenoOutput(calledVar);
            output.countSampleGeno();
            output.calculate();

            if (output.isValid()) {
                for (Sample sample : SampleManager.getList()) {
                    if (isCaseOnlyValid(sample)) {
                        int geno = output.getCalledVariant().getGenotype(sample.getIndex());

                        if (output.isQualifiedGeno(geno)) {
                            bwDetails.write(output.getString(sample));
                            bwDetails.newLine();

                            SampleVariantCount.update(output.getCalledVariant().isSnv(), 
                                    geno, sample.getIndex());
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private boolean isCaseOnlyValid(Sample sample) {
        if (VarGenoCommand.isCaseOnly) {
            if (sample.isCase()) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void outputSampleVariantCount() {
        try {
            for (Sample sample : SampleManager.getList()) {
                bwSampleVariantCount.write(sample.getName() + ",");
                bwSampleVariantCount.write(SampleVariantCount.getString(sample.getIndex()));
                bwSampleVariantCount.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running a list variant genotype function...";
    }
}
