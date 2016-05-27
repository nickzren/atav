package function.coverage.comparison;

import function.annotation.base.Gene;
import function.coverage.base.CoverageAnalysisBase;
import function.coverage.base.CoverageCommand;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public abstract class CoverageComparisonBase extends CoverageAnalysisBase {

    BufferedWriter bwCoverageSummaryByGene = null;
    final String coverageSummaryByGene = CommonCommand.outputPath + "coverage.summary.csv";

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwCoverageSummaryByGene = new BufferedWriter(new FileWriter(coverageSummaryByGene));
            bwCoverageSummaryByGene.write("Gene,Chr,AvgCase,AvgCtrl,AbsDiff,Length,CoverageImbalanceWarning");
            bwCoverageSummaryByGene.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            super.closeOutput();

            bwCoverageSummaryByGene.flush();
            bwCoverageSummaryByGene.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void beforeProcessDatabaseData() {
        super.beforeProcessDatabaseData();

        if (!CoverageCommand.isCoverageComparisonDoLinear
                && (SampleManager.getCaseNum() == 0 || SampleManager.getCtrlNum() == 0)) {
            ErrorManager.print("Error: this function does not support to run with case only or control only sample file. ");
        }
    }

    @Override
    public void processGene(Gene gene) {
        super.processGene(gene);

        outputGeneSummary(gene);
    }

    private void outputGeneSummary(Gene gene) {
        try {
            double avgCase = 0, avgCtrl = 0;
            for (Sample sample : SampleManager.getList()) {
                if (sample.isCase()) {
                    avgCase += aCoverage[gene.getIndex()][sample.getIndex()];
                } else {
                    avgCtrl += aCoverage[gene.getIndex()][sample.getIndex()];
                }
            }

            avgCase = avgCase / SampleManager.getCaseNum() / (double) gene.getLength();
            avgCtrl = avgCtrl / SampleManager.getCtrlNum() / (double) gene.getLength();
            StringBuilder sb = new StringBuilder();
            sb.append(gene.getName()).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(FormatManager.getSixDegitDouble(avgCase)).append(",");
            sb.append(FormatManager.getSixDegitDouble(avgCtrl)).append(",");
            double abs_diff = Math.abs(avgCase - avgCtrl);
            sb.append(FormatManager.getSixDegitDouble(abs_diff)).append(",");
            sb.append(gene.getLength()).append(",");
            if (abs_diff > CoverageCommand.geneCleanCutoff) {
                if (avgCase < avgCtrl) {
                    sb.append("bias against discovery");
                } else {
                    sb.append("bias for discovery");
                }

            } else {
                sb.append("none");
            }
            
            bwCoverageSummaryByGene.write(sb.toString());
            bwCoverageSummaryByGene.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }
}
