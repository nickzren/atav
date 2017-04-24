package function.coverage.comparison;

import function.coverage.base.SiteCoverage;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import utils.MathManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author qwang, nick
 */
public class SiteCoverageComparison extends CoverageComparisonBase {

    BufferedWriter bwSiteSummary = null;
    BufferedWriter bwSiteClean = null;
    public static BufferedWriter bwSitePruned = null;

    final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";
    final String cleanedSiteList = CommonCommand.outputPath + "site.clean.txt";
    final String sitePrunedFilePath = CommonCommand.outputPath + "site.pruned.csv";

    SiteClean siteClean = new SiteClean();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage,Site Coverage Case,Site Coverage Control");
            bwSiteSummary.newLine();

            bwSiteClean = new BufferedWriter(new FileWriter(cleanedSiteList));

            bwSitePruned = new BufferedWriter(new FileWriter(sitePrunedFilePath));
            bwSitePruned.write("Sample ID,Chr,Block Id,Start,End,Value");
            bwSitePruned.newLine();
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
            bwSiteClean.flush();
            bwSiteClean.close();
            bwSitePruned.flush();
            bwSitePruned.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        try {
            ThirdPartyToolManager.gzipFile(siteSummaryFilePath);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
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
            StringBuilder sb = new StringBuilder();
            for (int pos = 0; pos < exon.getLength(); pos++) {
                int caseCoverage = siteCoverage.getCaseSiteCov(pos);
                int ctrlCoverage = siteCoverage.getCtrlSiteCov(pos);
                int start = exon.getStartPosition() + pos;
                sb.append(gene.getName()).append(",");
                sb.append(gene.getChr()).append(",");
                sb.append(start).append(",");
                sb.append(caseCoverage + ctrlCoverage).append(",");
                sb.append(caseCoverage).append(",");
                sb.append(ctrlCoverage);
                writeToFile(sb.toString(), bwSiteSummary);
                sb.setLength(0);

                float caseAvg = MathManager.devide(caseCoverage, SampleManager.getCaseNum());
                float ctrlAvg = MathManager.devide(ctrlCoverage, SampleManager.getCtrlNum());
                float absDiff = MathManager.abs(caseAvg, ctrlAvg);
                siteClean.addSite(gene.getChr(), pos + exon.getStartPosition(), caseAvg, ctrlAvg, absDiff);
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
