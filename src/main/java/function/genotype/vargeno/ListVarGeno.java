package function.genotype.vargeno;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.SampleManager;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class ListVarGeno extends AnalysisBase4CalledVar {

    BufferedWriter bwGenotypes = null;
    final String genotypesFilePath = CommonCommand.outputPath + "genotypes.csv";

    @Override
    public void initOutput() {
        try {
            bwGenotypes = new BufferedWriter(new FileWriter(genotypesFilePath));
            bwGenotypes.write(VarGenoOutput.getTitle());
            bwGenotypes.newLine();
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
            bwGenotypes.flush();
            bwGenotypes.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (VarGenoCommand.isRunTier) {
            ThirdPartyToolManager.runNonTrioTier(genotypesFilePath);
        }

        if (VarGenoCommand.isMannWhitneyTest) {
            ThirdPartyToolManager.runMannWhitneyTest(genotypesFilePath);
        }
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
            VarGenoOutput output = new VarGenoOutput(calledVar);

            for (Sample sample : SampleManager.getList()) {
                byte geno = output.getCalledVariant().getGT(sample.getIndex());

                if (isCaseOnly(sample)
                        && output.isQualifiedGeno(geno)) {
                    bwGenotypes.write(output.getString(sample));
                    bwGenotypes.newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private boolean isCaseOnly(Sample sample) {
        if (VarGenoCommand.isCaseOnly) {
            return sample.isCase();
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "Start running list variant genotype function";
    }
}
