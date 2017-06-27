package function.coverage.comparison;

import function.coverage.base.SiteCoverage;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageCommand;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import utils.FormatManager;
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

    SiteClean siteClean = new SiteClean();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage,Site Coverage Case,Site Coverage Control,Covered Sample Binomial P (two sided)");
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

                double coveredSampleBinomialP = Data.NA;

                if (caseCoverage != 0
                        && ctrlCoverage != 0) {
                    coveredSampleBinomialP = MathManager.getBinomialTWOSIDED(
                            caseCoverage + ctrlCoverage,
                            caseCoverage,
                            MathManager.devide(SampleManager.getCaseNum(), SampleManager.getTotalSampleNum()));
                }

                if (GenotypeLevelFilterCommand.isMinCoveredSampleBinomialPValid(coveredSampleBinomialP)) {
                    int start = exon.getStartPosition() + pos;
                    sb.append(gene.getName()).append(",");
                    sb.append(gene.getChr()).append(",");
                    sb.append(start).append(",");
                    sb.append(caseCoverage + ctrlCoverage).append(",");
                    sb.append(caseCoverage).append(",");
                    sb.append(ctrlCoverage).append(",");
                    sb.append(FormatManager.getDouble(coveredSampleBinomialP));
                    writeToFile(sb.toString(), bwSiteSummary);
                    sb.setLength(0);

                    float caseAvg = MathManager.devide(caseCoverage, SampleManager.getCaseNum());
                    float ctrlAvg = MathManager.devide(ctrlCoverage, SampleManager.getCtrlNum());

                    if (CoverageCommand.isMinCoverageFractionValid(caseAvg)
                            && CoverageCommand.isMinCoverageFractionValid(ctrlAvg)) {
                        float covDiff = Data.NA;

                        if (CoverageCommand.isRelativeDifference) {
                            covDiff = MathManager.relativeDiff(caseAvg, ctrlAvg);
                        } else {
                            covDiff = MathManager.abs(caseAvg, ctrlAvg);
                        }

                        siteClean.addSite(gene.getChr(), pos + exon.getStartPosition(), caseAvg, ctrlAvg, covDiff);
                    }
                }
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
