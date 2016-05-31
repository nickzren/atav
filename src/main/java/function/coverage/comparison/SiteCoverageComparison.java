package function.coverage.comparison;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import utils.FormatManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author qwang, nick
 */
public class SiteCoverageComparison extends CoverageComparisonBase {

    BufferedWriter bwSiteSummary = null;
    BufferedWriter bwSiteClean = null;
    BufferedWriter bwGeneSummaryClean = null;

    final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";
    final String cleanedSiteList = CommonCommand.outputPath + "site.clean.txt";
    final String cleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";

    RegionClean regionClean = new RegionClean();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage,Site Coverage Case, Site Coverage Control");
            bwSiteSummary.newLine();

            bwSiteClean = new BufferedWriter(new FileWriter(cleanedSiteList));
            bwGeneSummaryClean = new BufferedWriter(new FileWriter(cleanedGeneSummaryList));
            bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgCase,AvgCtrl,AbsDiff,CleanedLength,CoverageImbalanceWarning");
            bwGeneSummaryClean.newLine();
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
            bwGeneSummaryClean.flush();
            bwGeneSummaryClean.close();
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
    public void processExon(HashMap<Integer, Integer> result, Gene gene, Exon exon) {
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
                bwSiteSummary.write(sb.toString());
                bwSiteSummary.newLine();
                sb.setLength(0);

                String name = gene.getName() + "_" + gene.getChr() + "_" + start;
                regionClean.addRegionToList(name, caseCoverage, ctrlCoverage);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputCleanedExonData() throws Exception {
        regionClean.initSortedRegionMap();

        printCleanedExonLog();
        
        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            String str = regionClean.getCleanedGeneStrBySite(gene);
            if (!str.isEmpty()) {
                bwSiteClean.write(str);
                bwSiteClean.newLine();
            }

            str = regionClean.getCleanedGeneSummaryStrBySite(gene);
            if (!str.isEmpty()) {
                bwGeneSummaryClean.write(str);
                bwGeneSummaryClean.newLine();
            }
        }
    }

    private void printCleanedExonLog() {
        LogManager.writeAndPrint("The total number of bases before pruning is "
                + FormatManager.getSixDegitDouble((double) regionClean.getTotalBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is "
                + FormatManager.getSixDegitDouble((double) regionClean.getTotalCleanedBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is "
                + FormatManager.getSixDegitDouble(100.0 - (double) regionClean.getTotalCleanedBases() / (double) regionClean.getTotalBases() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  "
                + FormatManager.getSixDegitDouble(regionClean.getAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  "
                + FormatManager.getSixDegitDouble(regionClean.getAllCoverage() * regionClean.getTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for cases after pruning is  "
                + FormatManager.getSixDegitDouble(regionClean.getCaseCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for cases after pruning is  "
                + FormatManager.getSixDegitDouble(regionClean.getCaseCoverage() * regionClean.getTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for controls after pruning is  "
                + FormatManager.getSixDegitDouble(regionClean.getCtrlCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for controls after pruning is  "
                + FormatManager.getSixDegitDouble(regionClean.getCtrlCoverage() * regionClean.getTotalBases() / 1000000.0) + " MB");
    }

    @Override
    public String toString() {
        return "It is running site coverage comparison function...";
    }
}
