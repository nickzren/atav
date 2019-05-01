package function.coverage.base;

import function.AnalysisBase;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.annotation.base.GeneManager;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import global.Data;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;
import utils.MathManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public abstract class CoverageAnalysisBase extends AnalysisBase {

    BufferedWriter bwSampleSummary = null;
    BufferedWriter bwCoverageDetails = null;
    final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    final String coverageDetailsFilePath = CommonCommand.outputPath + "coverage.details.csv";

    public int[] sampleCoverageCount = new int[SampleManager.getTotalSampleNum()];
    public int[][] geneSampleCoverage = new int[GeneManager.getGeneBoundaryList().size()][SampleManager.getTotalSampleNum()];

    @Override
    public void initOutput() {
        try {
            bwSampleSummary = new BufferedWriter(new FileWriter(sampleSummaryFilePath));
            bwSampleSummary.write("Sample,Total_Bases,Total_Covered_Base,%Overall_Bases_Covered,"
                    + "Total_Regions,Total_Covered_Regions,%Regions_Covered");
            bwSampleSummary.newLine();

            bwCoverageDetails = new BufferedWriter(new FileWriter(coverageDetailsFilePath));
            bwCoverageDetails.write("Sample,Gene,Chr,Length,Covered_Base,%Bases_Covered,Coverage_Status");
            bwCoverageDetails.newLine();

        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwSampleSummary.flush();
            bwSampleSummary.close();
            bwCoverageDetails.flush();
            bwCoverageDetails.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        ThirdPartyToolManager.gzipFile(coverageDetailsFilePath);
    }

    @Override
    public void beforeProcessDatabaseData() {
        if (GenotypeLevelFilterCommand.minCoverage == Data.NO_FILTER) {
            ErrorManager.print("--min-coverage option has to be used in this function.", ErrorManager.COMMAND_PARSING);
        }
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {
        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            count(gene);

            processGene(gene);

            outputSampleGeneSummary(gene);
        }

        outputSampleSummary();
    }

    public void writeToFile(String str, BufferedWriter bw) {
        try {
            if (!str.isEmpty()) {
                bw.write(str);
                bw.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void processGene(Gene gene) {
        for (Exon exon : gene.getExonList()) {
            HashMap<Integer, Integer> sampleCoveredLengthMap = CoverageManager.getSampleCoveredLengthMap(exon);
            accumulateCoverage(gene.getIndex(), sampleCoveredLengthMap);

            processExon(sampleCoveredLengthMap, gene, exon);
        }
    }

    public abstract void processExon(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon);

    public void accumulateCoverage(int geneIndex, HashMap<Integer, Integer> sampleCoveredLengthMap) {
        sampleCoveredLengthMap.keySet().stream().forEach((sampleId) -> {
            int sampleIndex = SampleManager.getIndexById(sampleId);
            geneSampleCoverage[geneIndex][sampleIndex] += sampleCoveredLengthMap.get(sampleId);
        });
    }

    private void outputSampleGeneSummary(Gene gene) {
        try {
            for (Sample sample : SampleManager.getList()) {
                StringJoiner sj = new StringJoiner(",");
                sj.add(sample.getName());
                sj.add(gene.getName());
                sj.add(gene.getChr());
                sj.add(FormatManager.getInteger(gene.getLength()));
                sj.add(FormatManager.getInteger(geneSampleCoverage[gene.getIndex()][sample.getIndex()]));

                double ratio = MathManager.devide(geneSampleCoverage[gene.getIndex()][sample.getIndex()], gene.getLength());
                sj.add(FormatManager.getDouble(ratio));

                int pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
                sj.add(FormatManager.getInteger(pass));

                writeToFile(sj.toString(), bwCoverageDetails);

                // count region per sample
                sampleCoverageCount[sample.getIndex()] += pass;
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputSampleSummary() {
        try {
            for (Sample sample : SampleManager.getList()) {
                StringJoiner sj = new StringJoiner(",");
                sj.add(sample.getName());
                sj.add(FormatManager.getInteger(GeneManager.getAllGeneBoundaryLength()));
                int totalSampleCov = getSampleCoverageByIndex(sample.getIndex());
                sj.add(FormatManager.getInteger(totalSampleCov));
                double ratio = MathManager.devide(totalSampleCov, GeneManager.getAllGeneBoundaryLength());
                sj.add(FormatManager.getDouble(ratio));
                sj.add(FormatManager.getInteger(GeneManager.getGeneBoundaryList().size()));
                int totalSampleRegionCovered = sampleCoverageCount[sample.getIndex()];
                sj.add(FormatManager.getInteger(totalSampleRegionCovered));
                ratio = MathManager.devide(totalSampleRegionCovered, GeneManager.getGeneBoundaryList().size());
                sj.add(FormatManager.getDouble(ratio));
                writeToFile(sj.toString(), bwSampleSummary);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private int getSampleCoverageByIndex(int sampleIndex) {
        int cov = 0;
        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            cov = cov + geneSampleCoverage[gene.getIndex()][sampleIndex];
        }
        return cov;
    }

    protected void count(Gene gene) {
        System.out.println("Processing " + (gene.getIndex() + 1) + " of "
                + GeneManager.getGeneBoundaryList().size()
                + ": " + gene.toString());
    }
}
