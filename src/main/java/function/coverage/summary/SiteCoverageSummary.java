package function.coverage.summary;

import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageAnalysisBase;
import global.Index;
import utils.CommonCommand;
import java.io.BufferedWriter;
import java.io.FileWriter;
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
    public void processExon(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        outputSiteSummary(gene, exon);
    }

    private void outputSiteSummary(Gene gene, Exon exon) {
        try {
            int[][] sampleSiteCoverage = CoverageManager.getSampleSiteCoverage(exon);
            StringBuilder sb = new StringBuilder();
            for (int pos = 0; pos < exon.getLength(); pos++) {
                sb.append(gene.getName()).append(",");
                sb.append(exon.getChrStr()).append(",");
                sb.append(exon.getStartPosition() + pos).append(",");
                sb.append(sampleSiteCoverage[Index.CASE][pos] + sampleSiteCoverage[Index.CTRL][pos]);
                bwSiteSummary.write(sb.toString());
                bwSiteSummary.newLine();
                sb.setLength(0);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running site coverage summary function...";
    }
}
