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

    private static int genoCount;
    private static char previousGeno;
    private static char currentGeno;

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
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
        if (VarGenoCommand.isRunVariantCount) {
            ThirdPartyToolManager.runVariantCount(genotypesFilePath);
        }
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            VarGenoOutput output = new VarGenoOutput(calledVar);

            boolean hasQualifiedVariant = false;
            StringBuilder gtArraySB = new StringBuilder();
            genoCount = 0;

            for (Sample sample : SampleManager.getList()) {
                byte geno = output.getCalledVariant().getGT(sample.getIndex());

                if (isCaseOnly(sample)
                        && output.isQualifiedGeno(geno)) {
                    hasQualifiedVariant = true;
                    bwGenotypes.write(output.getString(sample));
                    bwGenotypes.newLine();
                }

                add2GTArraySB(geno, gtArraySB);
            }

            if (VarGenoCommand.isIncludeHomRef && hasQualifiedVariant) {
                bwGenotypes.write(output.getJointedGenotypeString(gtArraySB.toString()));
                bwGenotypes.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void add2GTArraySB(byte geno, StringBuilder gtArraySB) {
        if (VarGenoCommand.isIncludeHomRef) {
            currentGeno = getGeno(geno);

            if (genoCount == 0) // first character geno
            {
                previousGeno = currentGeno;
                genoCount++;
            } else if (currentGeno == previousGeno) {
                genoCount++;
            } else {
                gtArraySB.append(genoCount).append(previousGeno);
                genoCount = 1;
                previousGeno = currentGeno;
            }
        }
    }

    private char getGeno(byte geno) {
        switch (geno) {
            case Index.HOM:
            case Index.HOM_MALE:
                return 'H';
            case Index.HET:
                return 'T';
            case Index.REF:
            case Index.REF_MALE:
                return 'R';
            default:
                return 'N';
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
