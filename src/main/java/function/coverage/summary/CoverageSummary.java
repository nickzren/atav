package function.coverage.summary;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.coverage.base.SampleStatistics;
import function.coverage.base.Exon;
import function.coverage.base.Gene;
import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author qwang
 */
public class CoverageSummary {

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

    public CoverageSummary() {
        super();

        if (GenotypeLevelFilterCommand.minCoverage == Data.NO_FILTER) {
            ErrorManager.print("--min-coverage option has to be used in this function.");
        }

    }

    public void DoExonSummary(SampleStatistics ss, int record, HashMap<Integer, Integer> result, Exon e) throws Exception {
        ss.print(record, result, e, bwCoverageDetailsByExon);
    }

    public void DoGeneSummary(SampleStatistics ss, int record) throws Exception {
        //do nothing for coverage summary
    }

    public void run() throws Exception {
        initOutput();
        SampleStatistics ss = new SampleStatistics(GeneManager.getGeneBoundaryList().size());
        ss.printMatrixHeader(bwSampleMatrixSummary, false);

        if (CoverageCommand.isByExon) {
            ss.printMatrixHeader(bwSampleExonMatrixSummary, true);
        }

        int record = 0;

        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            System.out.print("Processing " + (record + 1) + " of " + GeneManager.getGeneBoundaryList().size() + ":        " + gene.toString() + "                              \r");

            ss.setRecordName(record, gene.getName(), gene.getChr());
            ss.setLength(record, gene.getLength());

            for (Exon exon : gene.getExonList()) {
                HashMap<Integer, Integer> result = exon.getCoverage(GenotypeLevelFilterCommand.minCoverage);
                ss.accumulateCoverage(record, result);
                ss.printMatrixRowbyExon(record, result, exon, bwSampleExonMatrixSummary);
                DoExonSummary(ss, record, result, exon);
            }

            ss.print(record, bwSampleRegionSummary);
            ss.printMatrixRow(record, bwSampleMatrixSummary);
            DoGeneSummary(ss, record);

            record++;
        }

        ss.print(bwSampleSummary);
        closeOutput();
    }

    public void initOutput() throws Exception {
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
    }

    public void closeOutput() throws Exception {
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
    }
}
