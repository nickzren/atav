package function.coverage.summary;

import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageAnalysisBase;
import global.Index;
import utils.CommonCommand;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import utils.ErrorManager;

/**
 *
 * @author qwang, nick
 */
public class SiteCoverageSummary extends CoverageAnalysisBase {

    BufferedWriter bwSiteSummary = null;
    public final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage");
            bwSiteSummary.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            super.closeOutput();

            bwSiteSummary.flush();
            bwSiteSummary.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void processGene(Gene gene) {
        try {
            for (Exon exon : gene.getExonList()) {
                HashMap<Integer, Integer> result = CoverageManager.getCoverage(exon);
                ss.accumulateCoverage(gene.getIndex(), result);

                outputSiteSummary(gene, exon);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputSiteSummary(Gene gene, Exon exon) throws IOException {
        int[][] sampleSiteCoverage = CoverageManager.getSampleSiteCoverage(exon);
        for (int pos = 0; pos < exon.getLength(); pos++) {
            StringBuilder sb = new StringBuilder();
            sb.append(gene.getName()).append(",");
            sb.append(exon.getChrStr()).append(",");
            sb.append(exon.getStartPosition() + pos).append(",");
            sb.append(sampleSiteCoverage[Index.CASE][pos] + sampleSiteCoverage[Index.CTRL][pos]);
            sb.append("\n");
            bwSiteSummary.write(sb.toString());
        }
    }

    @Override
    public String toString() {
        return "It is running site coverage summary function...";
    }
}
