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

/**
 *
 * @author nick
 */
public abstract class CoverageAnalysisBase extends AnalysisBase {

    BufferedWriter bwSampleSummary = null;
    public final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    public SampleStatistics ss = new SampleStatistics();

    @Override
    public void initOutput() {
        try {
            bwSampleSummary = new BufferedWriter(new FileWriter(sampleSummaryFilePath));
            bwSampleSummary.write("Sample,Total_Bases,Total_Covered_Base,%Overall_Bases_Covered,"
                    + "Total_Regions,Total_Covered_Regions,%Regions_Covered");
            bwSampleSummary.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwSampleSummary.flush();
            bwSampleSummary.close();
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
            GeneManager.getGeneBoundaryList().stream().forEach((gene) -> {
                count(gene);

                processGene(gene);
            });

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
