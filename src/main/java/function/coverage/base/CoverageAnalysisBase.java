package function.coverage.base;

import function.AnalysisBase;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.annotation.base.GeneManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import global.Data;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;
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

    public int[] aSampleRegionCoverage = new int[SampleManager.getListSize()];
    public double[][] aCoverage = new double[GeneManager.getGeneBoundaryList().size()][SampleManager.getListSize()];

    @Override
    public void initOutput() {
        try {
            bwSampleSummary = new BufferedWriter(new FileWriter(sampleSummaryFilePath));
            bwSampleSummary.write("Sample,Total_Bases,Total_Covered_Base,%Overall_Bases_Covered,"
                    + "Total_Regions,Total_Covered_Regions,%Regions_Covered");
            bwSampleSummary.newLine();

            bwCoverageDetails = new BufferedWriter(new FileWriter(coverageDetailsFilePath));
            bwCoverageDetails.write("Sample,Gene/Transcript/Region,Chr,Length,"
                    + "Covered_Base,%Bases_Covered,Coverage_Status");
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
            ErrorManager.print("--min-coverage option has to be used in this function.");
        }
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {
        try {
            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                count(gene);

                processGene(gene);

                outputSampleGeneSummary(gene);
            }

            outputSampleSummary();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void processGene(Gene gene) {
        try {
            for (Exon exon : gene.getExonList()) {
                HashMap<Integer, Integer> result = CoverageManager.getCoverage(exon);
                accumulateCoverage(gene.getIndex(), result);
                
                processExon(result, gene, exon);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public abstract void processExon(HashMap<Integer, Integer> result, Gene gene, Exon exon);

    public void accumulateCoverage(int geneIndex, HashMap<Integer, Integer> result) {
        result.keySet().parallelStream().forEach((sampleId) -> {
            int sampleIndex = SampleManager.getIndexById(sampleId);
            aCoverage[geneIndex][sampleIndex]
                    = aCoverage[geneIndex][sampleIndex] + result.get(sampleId);
        });
    }

    private void outputSampleGeneSummary(Gene gene) throws Exception {
        for (Sample sample : SampleManager.getList()) {
            StringBuilder sb = new StringBuilder();
            sb.append(sample.getName()).append(",");
            sb.append(gene.getName()).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(gene.getLength()).append(",");
            sb.append((int) aCoverage[gene.getIndex()][sample.getIndex()]).append(",");

            double ratio = FormatManager.devide(
                    aCoverage[gene.getIndex()][sample.getIndex()], gene.getLength());
            sb.append(FormatManager.getSixDegitDouble(ratio)).append(",");

            int pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
            sb.append(pass);
            sb.append("\n");
            bwCoverageDetails.write(sb.toString());

            // count region per sample
            aSampleRegionCoverage[sample.getIndex()] += pass;
        }
    }
    
    private void outputSampleSummary() throws Exception {
        for (Sample sample : SampleManager.getList()) {
            StringBuilder sb = new StringBuilder();
            sb.append(sample.getName()).append(",");
            sb.append(GeneManager.getAllGeneBoundaryLength()).append(",");
            int totalSampleCov = getSampleCoverageByIndex(sample.getIndex());
            sb.append(totalSampleCov).append(",");
            double ratio = FormatManager.devide(totalSampleCov, GeneManager.getAllGeneBoundaryLength());
            sb.append(FormatManager.getSixDegitDouble(ratio)).append(",");
            sb.append(GeneManager.getGeneBoundaryList().size()).append(",");
            int totalSampleRegionCovered = aSampleRegionCoverage[sample.getIndex()];
            sb.append(totalSampleRegionCovered).append(",");
            ratio = FormatManager.devide(totalSampleRegionCovered, GeneManager.getGeneBoundaryList().size());
            sb.append(FormatManager.getSixDegitDouble(ratio));
            sb.append("\n");
            bwSampleSummary.write(sb.toString());
        }
    }
    
    private int getSampleCoverageByIndex(int sampleIndex) {
        int cov = 0;
        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            cov = cov + (int) aCoverage[gene.getIndex()][sampleIndex];
        }
        return cov;
    }

    protected void count(Gene gene) {
        System.out.print("Processing " + (gene.getIndex() + 1) + " of "
                + GeneManager.getGeneBoundaryList().size()
                + ": " + gene.toString() + "                              \r");
    }
}
