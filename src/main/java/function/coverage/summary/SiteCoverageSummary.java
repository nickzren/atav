package function.coverage.summary;

import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageAnalysisBase;
import function.coverage.base.SiteCoverage;
import utils.CommonCommand;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.ErrorManager;
import utils.FormatManager;

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
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage,"
                    + "Case DP Bin 10,Case DP Bin 20,Case DP Bin 30,Case DP Bin 50,Case DP Bin 200,"
                    + "Ctrl DP Bin 10,Ctrl DP Bin 20,Ctrl DP Bin 30,Ctrl DP Bin 50,Ctrl DP Bin 200");
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
            SiteCoverage siteCoverage = CoverageManager.getSiteCoverage(exon);
            for (int pos = 0; pos < exon.getLength(); pos++) {
                StringJoiner sj = new StringJoiner(",");
                sj.add(gene.getName());
                sj.add(exon.getChrStr());
                sj.add(FormatManager.getInteger(exon.getStartPosition() + pos));
                int siteTotalCov = siteCoverage.getCaseSiteCov(pos)
                        + siteCoverage.getCtrlSiteCov(pos);
                sj.add(FormatManager.getInteger(siteTotalCov));
                writeToFile(sj.toString(), bwSiteSummary);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running site coverage summary function";
    }
}
