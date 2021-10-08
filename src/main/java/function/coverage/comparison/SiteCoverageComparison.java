package function.coverage.comparison;

import function.coverage.base.SiteCoverage;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageCommand;
import function.cohort.base.SampleManager;
import global.Data;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author qwang, nick
 */
public class SiteCoverageComparison extends CoverageComparisonBase {

    BufferedWriter bwSiteSummary = null;
    BufferedWriter bwSiteClean = null;

    final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";
    final String cleanedSiteList = CommonCommand.outputPath + "site.clean.txt";

    SiteClean siteClean = new SiteClean();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            if (CoverageCommand.isIncludeSiteSummary) {
                bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
                bwSiteSummary.write("Gene,Chr,Pos,"
                        + "Case DP Bin 0,Case DP Bin 10,"
                        + "Ctrl DP Bin 0,Ctrl DP Bin 10");
                bwSiteSummary.newLine();
            }

            bwSiteClean = new BufferedWriter(new FileWriter(cleanedSiteList));
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            super.closeOutput();

            if (CoverageCommand.isIncludeSiteSummary) {
                bwSiteSummary.flush();
                bwSiteSummary.close();
            }
            bwSiteClean.flush();
            bwSiteClean.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void afterProcessDatabaseData() {
        try {
            outputCleanedSiteData();
        } catch (Exception e) {
            ErrorManager.send(e);
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
                int start = exon.getStartPosition() + pos;

                int caseSiteCov = siteCoverage.getCaseSiteCov(pos);
                int ctrlSiteCov = siteCoverage.getCtrlSiteCov(pos);
                float caseAvg = MathManager.devide(caseSiteCov, SampleManager.getCaseNum());
                float ctrlAvg = MathManager.devide(ctrlSiteCov, SampleManager.getCtrlNum());

                if (CoverageCommand.isIncludeSiteSummary) {
                    StringJoiner sj = new StringJoiner(",");
                    sj.add(gene.getName());
                    sj.add(gene.getChr());
                    sj.add(FormatManager.getInteger(start));
                    sj.add(FormatManager.getInteger(SampleManager.getCaseNum() - caseSiteCov));
                    sj.add(FormatManager.getInteger(siteCoverage.getCaseSiteCov(Index.DP_BIN_10, pos)));
                    sj.add(FormatManager.getInteger(SampleManager.getCtrlNum() - ctrlSiteCov));
                    sj.add(FormatManager.getInteger(siteCoverage.getCtrlSiteCov(Index.DP_BIN_10, pos)));
                    writeToFile(sj.toString(), bwSiteSummary);
                }

                float covDiff = Data.FLOAT_NA;

                if (CoverageCommand.isRelativeDifference) {
                    covDiff = MathManager.relativeDiff(caseAvg, ctrlAvg);
                } else {
                    covDiff = MathManager.abs(caseAvg, ctrlAvg);
                }

                siteClean.addSite(gene.getChr(), pos + exon.getStartPosition(), caseAvg, ctrlAvg, covDiff);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputCleanedSiteData() throws Exception {
        siteClean.initCleanedSiteMap();
        siteClean.outputLog();

        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            writeToFile(siteClean.getCleanedGeneStrBySite(gene), bwSiteClean);

            writeToFile(siteClean.getCleanedGeneSummaryStrBySite(gene), bwGeneSummaryClean);
        }
    }

    @Override
    public String toString() {
        return "Start running site coverage comparison function";
    }
}
