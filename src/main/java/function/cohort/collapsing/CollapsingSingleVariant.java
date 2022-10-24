package function.cohort.collapsing;

import function.annotation.base.AnnotationLevelFilterCommand;
import function.cohort.base.CalledVariant;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class CollapsingSingleVariant extends CollapsingBase {

    BufferedWriter bwGenotypes = null;
    final String genotypesFilePath = CommonCommand.outputPath + "genotypes.csv";

    static int genoCount;
    static char previousGeno;
    static char currentGeno;

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwGenotypes = new BufferedWriter(new FileWriter(genotypesFilePath));
            bwGenotypes.write(CollapsingOutput.getHeader());
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
    public void doAfterCloseOutput() {
        super.doAfterCloseOutput();

        if (CollapsingCommand.isMannWhitneyTest) {
            ThirdPartyToolManager.runMannWhitneyTest(genotypesFilePath);
        }

        if (CommonCommand.gzip) {
            ThirdPartyToolManager.gzipFile(genotypesFilePath);
        }
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            CollapsingOutput output = new CollapsingOutput(calledVar);

            ArrayList<CollapsingSummary> summaryList = new ArrayList<>();

            initSummaryList(output, summaryList);

            if (!summaryList.isEmpty()) {
                processOutput4Summary(output, summaryList);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initSummaryList(CollapsingOutput output, ArrayList<CollapsingSummary> summaryList) {
        if (!CollapsingCommand.regionBoundaryFile.isEmpty()) {
            // region summary
            output.initRegionBoundaryNameSet();

            for (String regionName : output.regionBoundaryNameList) {
                summaryMap.putIfAbsent(regionName, new CollapsingGeneSummary(regionName));
                summaryList.add(summaryMap.get(regionName));
            }
        } else if (!AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
            // transcript summary
            for (int id : output.getCalledVariant().getTranscriptSet()) {
                String idStr = String.valueOf(id);
                summaryMap.putIfAbsent(idStr, new CollapsingTranscriptSummary(idStr));
                summaryList.add(summaryMap.get(idStr));
            }
        } else {
            // gene summary
            for (String geneName : output.getCalledVariant().getGeneSet()) {
                if (!geneName.equals(Data.STRING_NA)) {
                    summaryMap.putIfAbsent(geneName, new CollapsingGeneSummary(geneName));
                    summaryList.add(summaryMap.get(geneName));
                }
            }
        }
    }

    private void processOutput4Summary(CollapsingOutput output,
            ArrayList<CollapsingSummary> summaryList) {
        try {
            boolean hasQualifiedVariant = false;

            for (Sample sample : SampleManager.getList()) {
                byte geno = output.getCalledVariant().getGT(sample.getIndex());
                if (output.isQualifiedGeno(geno)) {
                    hasQualifiedVariant = true;

                    for (CollapsingSummary summary : summaryList) {
                        summary.countQualifiedVariantBySample(geno, sample.getIndex());
                    }

                    output.calculateLooAF(sample);
                    outputQualifiedVariant(output, sample);
                }
            }

            // only count qualified variant once per gene or region
            if (hasQualifiedVariant) {
                for (CollapsingSummary summary : summaryList) {
                    summary.updateVariantCount(output.getCalledVariant().isSNV());
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputQualifiedVariant(CollapsingOutput output,
            Sample sample) throws Exception {
        bwGenotypes.write(output.getStringJoiner(sample).toString());
        bwGenotypes.newLine();
    }

    @Override
    public String toString() {
        return "Start running collapsing function";
    }
}
