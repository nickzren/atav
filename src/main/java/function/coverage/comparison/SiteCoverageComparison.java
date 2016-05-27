package function.coverage.comparison;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
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
import java.util.HashSet;
import utils.FormatManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author qwang, nick
 */
public class SiteCoverageComparison extends CoverageComparisonBase {

    BufferedWriter bwSiteSummary = null;

    final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";

    RegionClean ec = new RegionClean();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage,Site Coverage Case, Site Coverage Control");
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
            outputCleanedExonList();
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
                sb.append("\n");
                bwSiteSummary.write(sb.toString());
                sb.setLength(0);

                String name = gene.getName() + "_" + gene.getChr() + "_" + start;
                ec.addRegionToList(name, caseCoverage, ctrlCoverage);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputCleanedExonList() throws Exception {
        final String cleanedSiteList = CommonCommand.outputPath + "site.clean.txt";
        final String cleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";
        BufferedWriter bwSiteClean = new BufferedWriter(new FileWriter(cleanedSiteList));
        BufferedWriter bwGeneSummaryClean = new BufferedWriter(new FileWriter(cleanedGeneSummaryList));
        bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgCase,AvgCtrl,AbsDiff,CleanedLength,CoverageImbalanceWarning");
        bwGeneSummaryClean.newLine();

        //make sure the list has included all data and sortd.
        ec.FinalizeRegionList();

        double cutoff = ec.GetCutoff();
        LogManager.writeAndPrint("\nThe automated cutoff value for absolute mean coverage difference for sites is " + Double.toString(cutoff));
        if (CoverageCommand.siteCleanCutoff >= 0) {
            cutoff = CoverageCommand.siteCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value " + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
        }
        HashSet<String> cleanedList = ec.getRegionCleanList(cutoff);

        LogManager.writeAndPrint("The total number of bases before pruning is " + FormatManager.getSixDegitDouble((double) ec.GetTotalBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is " + FormatManager.getSixDegitDouble((double) ec.GetTotalCleanedBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is " + FormatManager.getSixDegitDouble(100.0 - (double) ec.GetTotalCleanedBases() / (double) ec.GetTotalBases() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  " + FormatManager.getSixDegitDouble(ec.GetAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  " + FormatManager.getSixDegitDouble(ec.GetAllCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for cases after pruning is  " + FormatManager.getSixDegitDouble(ec.GetCaseCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for cases after pruning is  " + FormatManager.getSixDegitDouble(ec.GetCaseCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for controls after pruning is  " + FormatManager.getSixDegitDouble(ec.GetControlCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for controls after pruning is  " + FormatManager.getSixDegitDouble(ec.GetControlCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            String str = ec.getCleanedGeneStringSite(gene, cleanedList);
            if (!str.isEmpty()) {
                bwSiteClean.write(str);
                bwSiteClean.newLine();
            }

            str = ec.getCleanedGeneSummaryString(gene, cleanedList, true);
            if (!str.isEmpty()) {
                bwGeneSummaryClean.write(str);
                bwGeneSummaryClean.newLine();
            }
        }

        bwSiteClean.flush();
        bwSiteClean.close();
        bwGeneSummaryClean.flush();
        bwGeneSummaryClean.close();
    }

    @Override
    public String toString() {
        return "It is running site coverage comparison function...";
    }
}
