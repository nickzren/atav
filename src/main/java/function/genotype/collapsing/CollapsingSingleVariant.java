package function.genotype.collapsing;

import function.genotype.vargeno.SampleVariantCount;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class CollapsingSingleVariant extends CollapsingBase {

    BufferedWriter bwGenotypes = null;
    final String genotypesFilePath = CommonCommand.outputPath + "genotypes.csv";

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwGenotypes = new BufferedWriter(new FileWriter(genotypesFilePath));
            bwGenotypes.write(CollapsingOutput.getTitle());
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

            bwGenotypes.flush();
            bwGenotypes.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            CollapsingOutput output = new CollapsingOutput(calledVar);

            ArrayList<CollapsingSummary> summaryList = new ArrayList<>();

            initSummaryList(output, summaryList);

            if (!summaryList.isEmpty()) {
                output.countSampleGeno();

                output.calculate();

                if (output.isValid()) {
                    processOutput4Summary(output, summaryList);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initSummaryList(CollapsingOutput output, ArrayList<CollapsingSummary> summaryList) {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            // gene summary
            for (String geneName : output.getCalledVariant().getGeneSet()) {
                if (!geneName.equals("NA")) {
                    updateGeneSummaryMap(geneName);
                    summaryList.add(summaryMap.get(geneName));
                }
            }
        } else {
            // region summary
            output.initRegionBoundaryNameSet();

            for (String regionName : output.regionBoundaryNameSet) {
                updateRegionSummaryMap(regionName);
                summaryList.add(summaryMap.get(regionName));
            }
        }
    }

    private void processOutput4Summary(CollapsingOutput output,
            ArrayList<CollapsingSummary> summaryList) {
        try {
            boolean isVariantQualified = false;

            for (Sample sample : SampleManager.getList()) {
                output.calculateLooFreq(sample);

                if (output.isMaxLooMafValid()) {
                    int geno = output.getCalledVariant().getGenotype(sample.getIndex());

                    if (output.isQualifiedGeno(geno)) {
                        for (CollapsingSummary summary : summaryList) {
                            summary.updateSampleVariantCount4SingleVar(sample.getIndex());
                        }

                        outputQualifiedVariant(output, sample);

                        isVariantQualified = true;
                    }
                }
            }

            // only count qualified variant once per gene or region
            if (isVariantQualified) {
                for (CollapsingSummary summary : summaryList) {
                    summary.updateVariantCount(output);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputQualifiedVariant(CollapsingOutput output,
            Sample sample) throws Exception {
        bwGenotypes.write(output.getString(sample));
        bwGenotypes.newLine();

        SampleVariantCount.update(output.getCalledVariant().isSnv(),
                output.getCalledVariant().getGenotype(sample.getIndex()),
                sample.getIndex());
    }

    @Override
    public String toString() {
        return "Start running collapsing function...";
    }
}
