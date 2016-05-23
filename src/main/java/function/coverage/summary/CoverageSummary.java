package function.coverage.summary;

import function.AnalysisBase;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.coverage.base.CoverageManager;
import function.coverage.base.SampleStatistics;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

/**
 *
 * @author qwang
 */
public class CoverageSummary extends AnalysisBase {

    public BufferedWriter bwSampleSummary = null;
    public BufferedWriter bwSampleRegionSummary = null;
    public BufferedWriter bwSampleMatrixSummary = null;
    public BufferedWriter bwSampleExonMatrixSummary = null;
    public BufferedWriter bwCoverageDetailsByExon = null;
    public BufferedWriter bwCoverageSummaryByExon = null;
    public BufferedWriter bwCoverageSummaryByGene = null;

    public final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    public final String coverageDetailsFilePath = CommonCommand.outputPath + "coverage.details.csv";
    public final String coverageDetailsByExonFilePath = CommonCommand.outputPath + "coverage.details.by.exon.csv";
    public final String coverageMatrixFilePath = CommonCommand.outputPath + "coverage.details.matrix.csv";
    public final String coverageExonMatrixFilePath = CommonCommand.outputPath + "coverage.details.matrix.by.exon.csv";

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

            bwSampleMatrixSummary = new BufferedWriter(new FileWriter(coverageMatrixFilePath));

            if (CoverageCommand.isByExon) {
                if (CoverageCommand.isCoverageSummary) {
                    bwCoverageDetailsByExon = new BufferedWriter(new FileWriter(coverageDetailsByExonFilePath));
                    bwCoverageDetailsByExon.write("Sample,Gene/Transcript,Chr,Exon,Start_Position, Stop_Position,Length,Covered_Base,%Bases_Covered,Coverage_Status");
                    bwCoverageDetailsByExon.newLine();
                }
                bwSampleExonMatrixSummary = new BufferedWriter(new FileWriter(coverageExonMatrixFilePath));
            }
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
            bwSampleMatrixSummary.flush();
            bwSampleMatrixSummary.close();

            if (CoverageCommand.isByExon) {
                if (CoverageCommand.isCoverageSummary) {
                    bwCoverageDetailsByExon.flush();
                    bwCoverageDetailsByExon.close();
                }
                bwSampleExonMatrixSummary.flush();
                bwSampleExonMatrixSummary.close();
            }
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

    public void DoExonSummary(SampleStatistics ss, HashMap<Integer, Integer> result, Gene gene, Exon exon) throws Exception {
        ss.print(result, gene, exon, bwCoverageDetailsByExon);
    }

    public void DoGeneSummary(SampleStatistics ss, Gene gene, int record) throws Exception {
        //do nothing for coverage summary
    }

    @Override
    public void processDatabaseData() {
        try {
            SampleStatistics ss = new SampleStatistics(GeneManager.getGeneBoundaryList().size());
            ss.printMatrixHeader(bwSampleMatrixSummary, false);

            if (CoverageCommand.isByExon) {
                ss.printMatrixHeader(bwSampleExonMatrixSummary, true);
            }

            int record = 0;

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                System.out.print("Processing " + (record + 1) + " of " + GeneManager.getGeneBoundaryList().size() + ": " + gene.toString() + "                              \r");

                ss.setLength(record, gene.getLength());

                for (Exon exon : gene.getExonList()) {
                    HashMap<Integer, Integer> result = CoverageManager.getCoverage(exon);
                    ss.accumulateCoverage(record, result);
                    ss.printMatrixRowbyExon(record, result, gene, exon, bwSampleExonMatrixSummary);
                    DoExonSummary(ss, result, gene, exon);
                }

                ss.print(record, gene, bwSampleRegionSummary);
                ss.printMatrixRow(record, gene, bwSampleMatrixSummary);
                DoGeneSummary(ss, gene, record);

                record++;
            }

            ss.print(bwSampleSummary);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running coverage summary function...";
    }
}
