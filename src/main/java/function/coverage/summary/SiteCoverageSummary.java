package function.coverage.summary;

import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageAnalysisBase;
import utils.CommonCommand;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
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
                int SiteStart = exon.getStartPosition();

                ArrayList<int[]> SiteCoverage = CoverageManager.getCoverageForSites(exon);

                for (int pos = 0; pos < SiteCoverage.get(0).length; pos++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(gene.getName()).append(",").append(exon.getChrStr()).append(",");
                    int total_coverage = SiteCoverage.get(0)[pos] + SiteCoverage.get(1)[pos];
                    sb.append(SiteStart + pos).append(",").append(total_coverage);
                    sb.append("\n");
                    bwSiteSummary.write(sb.toString());
                }
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
