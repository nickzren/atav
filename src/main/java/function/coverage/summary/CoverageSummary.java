package function.coverage.summary;

import function.AnalysisBase;
import function.annotation.base.GeneManager;
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
                    ss.print(result, gene, exon, bwCoverageDetailsByExon);
                }

                ss.print(gene, bwSampleRegionSummary);
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
