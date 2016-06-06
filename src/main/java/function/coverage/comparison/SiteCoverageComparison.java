package function.coverage.comparison;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.genotype.base.SampleManager;
import global.Index;
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

    final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";
    final String cleanedSiteList = CommonCommand.outputPath + "site.clean.txt";

    RegionClean regionClean = new RegionClean();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage,Site Coverage Case, Site Coverage Control");
            bwSiteSummary.newLine();

            bwSiteClean = new BufferedWriter(new FileWriter(cleanedSiteList));
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
            outputCleanedExonData();
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
            int[][] sampleSiteCoverage = CoverageManager.getSampleSiteCoverage(exon);
            StringBuilder sb = new StringBuilder();
            for (int pos = 0; pos < exon.getLength(); pos++) {
                int caseCoverage = sampleSiteCoverage[Index.CASE][pos];
                int ctrlCoverage = sampleSiteCoverage[Index.CTRL][pos];
                int start = exon.getStartPosition() + pos;
                sb.append(gene.getName()).append(",");
                sb.append(gene.getChr()).append(",");
                sb.append(start).append(",");
                sb.append(caseCoverage + ctrlCoverage).append(",");
                sb.append(caseCoverage).append(",");
                sb.append(ctrlCoverage);
                writeToFile(sb.toString(), bwSiteSummary);
                sb.setLength(0);

                String name = gene.getName() + "_" + gene.getChr() + "_" + start;
                double caseAvg = MathManager.devide(caseCoverage, SampleManager.getCaseNum());
                double ctrlAvg = MathManager.devide(ctrlCoverage, SampleManager.getCtrlNum());
                double absDiff = MathManager.abs(caseAvg, ctrlAvg);
                regionClean.addExon(name, caseAvg, ctrlAvg, absDiff, 1);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputCleanedExonData() throws Exception {
        regionClean.initCleanedRegionMap();
        regionClean.outputLog();

        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            writeToFile(regionClean.getCleanedGeneStrBySite(gene), bwSiteClean);

            writeToFile(regionClean.getCleanedGeneSummaryStrBySite(gene), bwGeneSummaryClean);
        }
    }

    @Override
    public String toString() {
        return "It is running site coverage comparison function...";
    }
}
