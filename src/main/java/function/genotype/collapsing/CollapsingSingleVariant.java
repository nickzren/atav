package function.genotype.collapsing;

import function.genotype.vargeno.SampleVariantCount;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.variant.base.Variant;
import global.Data;
import global.Index;
import function.genotype.base.SampleManager;
import utils.CommandValue;
import utils.ErrorManager;
import utils.FormatManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class CollapsingSingleVariant extends CollapsingBase {

    BufferedWriter bwQualifiedVariant = null;
    BufferedWriter bwMissingVariant = null;
    BufferedWriter bwGenotypes = null;
    final String genotypesFilePath = CommandValue.outputPath + "genotypes.csv";
    final String qualifiedVariantFilePath = CommandValue.outputPath + "qualified.variant.txt";
    final String missingVariantFilePath = CommandValue.outputPath + "missing.variant.txt";

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwQualifiedVariant = new BufferedWriter(new FileWriter(qualifiedVariantFilePath));

            bwMissingVariant = new BufferedWriter(new FileWriter(missingVariantFilePath));

            bwGenotypes = new BufferedWriter(new FileWriter(genotypesFilePath));
            bwGenotypes.write(CollapsingOutput.title);
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
            super.closeOutput();

            bwQualifiedVariant.flush();
            bwQualifiedVariant.close();
            bwMissingVariant.flush();
            bwMissingVariant.close();
            bwGenotypes.flush();
            bwGenotypes.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            if (isMissingRateValid(calledVar)) {
                CollapsingOutput output = new CollapsingOutput(calledVar);

                output.countSampleGenoCov();

                output.calculate();

                if (output.isValid()) {
                    for (String geneName : calledVar.getGeneSet()) {
                        if (!geneName.equals("NA")) {
                            updateSummaryTable(geneName);

                            output.geneName = geneName;

                            processOutput(output);
                        }
                    }
                }
            } else {
                outputMissingVariant(calledVar);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void processOutput(CollapsingOutput output) {
        try {
            CollapsingSummary summary = summaryTable.get(output.geneName);
            boolean countOnce = false;

            for (Sample sample : SampleManager.getList()) {
                output.calculateLooFreq(sample);

                if (output.isLooFreqValid()) {
                    int geno = output.getCalledVariant().getGenotype(sample.getIndex());

                    if (output.isQualifiedGeno(geno)) {
                        summary.updateSampleVariantCount4SingleVar(sample.getIndex());

                        if (!countOnce) {
                            summary.updateVariantCount(output);
                            countOnce = true;
                        }

                        outputQualifiedVariant(output, sample);
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputQualifiedVariant(CollapsingOutput output,
            Sample sample) throws Exception {
        bwQualifiedVariant.write(output.geneName + "\t");
        bwQualifiedVariant.write(output.getCalledVariant().getVariantIdStr());

        int geno = output.getCalledVariant().getGenotype(sample.getIndex());
        bwQualifiedVariant.write("\t" + sample.getName()
                + " (" + FormatManager.getDouble(output.looMaf) + ")");

        output.initGenoType(geno);
        output.initPhenoType((int) sample.getPheno());
        output.sampleName = sample.getName();
        outputGenotypes(output, sample);

        bwQualifiedVariant.newLine();

        SampleVariantCount.update(output.getCalledVariant().isSnv(),
                output.getCalledVariant().getGenotype(sample.getIndex()),
                sample.getIndex());
    }

    private void outputGenotypes(CollapsingOutput output, Sample sample) throws Exception {
        bwGenotypes.write(output.getString(sample));
        bwGenotypes.newLine();
    }

    private void outputMissingVariant(Variant var) throws Exception {
        bwMissingVariant.write(var.getVariantIdStr());
        bwMissingVariant.newLine();
    }

    private boolean isMissingRateValid(CalledVariant calledVar) {
        if (CommandValue.varMissingRate == Double.MAX_VALUE) {
            return true;
        }

        int missing = 0;
        for (Sample sample : SampleManager.getList()) {
            if (calledVar.getGenotype(sample.getIndex()) == Data.NA) {
                missing++;
            }
        }

        double missingRate = FormatManager.devide(missing, SampleManager.getListSize());

        if (missingRate <= CommandValue.varMissingRate) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "It is running a collapsing function...";
    }
}
