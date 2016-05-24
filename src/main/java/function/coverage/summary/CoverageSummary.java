package function.coverage.summary;

import function.AnalysisBase;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageManager;
import function.coverage.base.SampleStatistics;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageCommand;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import utils.FormatManager;

/**
 *
 * @author qwang, nick
 */
public class CoverageSummary extends AnalysisBase {

    public BufferedWriter bwSampleSummary = null;
    public BufferedWriter bwSampleRegionSummary = null;
    public BufferedWriter bwCoverageDetailsByExon = null;
    public BufferedWriter bwCoverageSummaryByExon = null;
    public BufferedWriter bwCoverageSummaryByGene = null;

    public final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    public final String coverageDetailsFilePath = CommonCommand.outputPath + "coverage.details.csv";
    public final String coverageDetailsByExonFilePath = CommonCommand.outputPath + "coverage.details.by.exon.csv";

    @Override
    public void initOutput() {
        try {
            bwSampleSummary = new BufferedWriter(new FileWriter(sampleSummaryFilePath));
            bwSampleSummary.write("Sample,Total_Bases,Total_Covered_Base,%Overall_Bases_Covered,"
                    + "Total_Regions,Total_Covered_Regions,%Regions_Covered");
            bwSampleSummary.newLine();

            bwSampleRegionSummary = new BufferedWriter(new FileWriter(coverageDetailsFilePath));
            bwSampleRegionSummary.write("Sample,Gene/Transcript/Region,Chr,Length,"
                    + "Covered_Base,%Bases_Covered,Coverage_Status");
            bwSampleRegionSummary.newLine();

            bwCoverageDetailsByExon = new BufferedWriter(new FileWriter(coverageDetailsByExonFilePath));
            bwCoverageDetailsByExon.write("Sample,Gene/Transcript,Chr,Exon,Start_Position, Stop_Position,Length,Covered_Base,%Bases_Covered,Coverage_Status");
            bwCoverageDetailsByExon.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwSampleSummary.flush();
            bwSampleSummary.close();
            bwSampleRegionSummary.flush();
            bwSampleRegionSummary.close();
            bwCoverageDetailsByExon.flush();
            bwCoverageDetailsByExon.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
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
            SampleStatistics ss = new SampleStatistics(GeneManager.getGeneBoundaryList().size());

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                System.out.print("Processing " + (gene.getIndex() + 1) + " of "
                        + GeneManager.getGeneBoundaryList().size() + ": " + gene.toString() + "                              \r");

                for (Exon exon : gene.getExonList()) {
                    HashMap<Integer, Integer> result = CoverageManager.getCoverage(exon);
                    ss.accumulateCoverage(gene, result);
                    outputCoverageDetailsByExon(result, gene, exon);
                }

                ss.print(gene, bwSampleRegionSummary);
            }

            ss.print(bwSampleSummary);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputCoverageDetailsByExon(HashMap<Integer, Integer> result,
            Gene gene, Exon e) throws IOException {
        Set<Integer> samples = result.keySet();
        for (Sample sample : SampleManager.getList()) {
            StringBuilder sb = new StringBuilder();
            sb.append(sample.getName()).append(",");
            sb.append(gene.getName()).append(",");
            sb.append(e.getChrStr()).append(",");
            sb.append(e.getIdStr()).append(",");
            sb.append(e.getStartPosition()).append(",");
            sb.append(e.getEndPosition()).append(",");
            sb.append(e.getLength()).append(",");

            int cov = 0;
            if (samples.contains(sample.getId())) {
                cov = result.get(sample.getId());

            }
            sb.append(cov).append(",");

            int pass;
            if (e.getLength() > 0) {
                double ratio = FormatManager.devide(cov, e.getLength());
                sb.append(FormatManager.getSixDegitDouble(ratio)).append(",");
                pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
            } else {
                sb.append("NA").append(",");
                pass = 0;
            }
            sb.append(pass);

            sb.append("\n");

            bwCoverageDetailsByExon.write(sb.toString());
        }
    }

    @Override
    public String toString() {
        return "It is running coverage summary function...";
    }
}
