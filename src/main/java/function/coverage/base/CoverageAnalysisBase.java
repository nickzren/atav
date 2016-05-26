package function.coverage.base;

import function.AnalysisBase;
import function.annotation.base.Gene;
import function.annotation.base.GeneManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public abstract class CoverageAnalysisBase extends AnalysisBase {

    BufferedWriter bwSampleSummary = null;
    BufferedWriter bwSampleRegionSummary = null;
    final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    final String coverageDetailsFilePath = CommonCommand.outputPath + "coverage.details.csv";

    public SampleStatistics ss = new SampleStatistics();

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

                ss.print(gene, bwSampleRegionSummary);
            }

            ss.print(bwSampleSummary);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public abstract void processGene(Gene gene);

    protected void count(Gene gene) {
        System.out.print("Processing " + (gene.getIndex() + 1) + " of "
                + GeneManager.getGeneBoundaryList().size()
                + ": " + gene.toString() + "                              \r");
    }
}
