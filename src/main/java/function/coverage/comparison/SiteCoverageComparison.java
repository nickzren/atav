package function.coverage.comparison;

import function.AnalysisBase;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.coverage.base.CoverageManager;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.SampleStatistics;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import utils.FormatManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author qwang, nick
 */
public class SiteCoverageComparison extends AnalysisBase {

    final String CleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";
    final String coverageSummaryByGene = CommonCommand.outputPath + "coverage.summary.csv";
    final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";
    BufferedWriter bwSiteSummary = null;
    BufferedWriter bwCoverageSummaryByGene = null;
    BufferedWriter bwSampleSummary = null;
    RegionClean ec = new RegionClean();

    @Override
    public void initOutput() {
        try {
            bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage,Site Coverage Case, Site Coverage Control");
            bwSiteSummary.newLine();

            bwSampleSummary = new BufferedWriter(new FileWriter(sampleSummaryFilePath));
            bwSampleSummary.write("Sample,Total_Bases,Total_Covered_Base,%Overall_Bases_Covered,"
                    + "Total_Regions,Total_Covered_Regions,%Regions_Covered");
            bwSampleSummary.newLine();

            bwCoverageSummaryByGene = new BufferedWriter(new FileWriter(coverageSummaryByGene));
            bwCoverageSummaryByGene.write("Gene,Chr,AvgCase,AvgCtrl,AbsDiff,Length,CoverageImbalanceWarning");
            bwCoverageSummaryByGene.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwSiteSummary.flush();
            bwSiteSummary.close();
            bwSampleSummary.flush();
            bwSampleSummary.close();
            bwCoverageSummaryByGene.flush();
            bwCoverageSummaryByGene.close();
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
    public void beforeProcessDatabaseData() {
        if (GenotypeLevelFilterCommand.minCoverage == Data.NO_FILTER) {
            ErrorManager.print("--min-coverage option has to be used in this function.");
        }

        int sampleSize = SampleManager.getListSize();
        if (sampleSize == SampleManager.getCaseNum() || sampleSize == SampleManager.getCtrlNum()) {
            ErrorManager.print("Error: this function is not supposed to run with case only or control only sample file. ");
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
    public void processDatabaseData() {
        try {
            SampleStatistics ss = new SampleStatistics(GeneManager.getGeneBoundaryList().size());

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                System.out.print("Processing " + (gene.getIndex() + 1) + " of "
                        + GeneManager.getGeneBoundaryList().size() + ": " + gene.toString() + "                              \r");

                for (Exon exon : gene.getExonList()) {
                    HashMap<Integer, Integer> result = CoverageManager.getCoverage(exon);
                    ss.accumulateCoverage(gene, result);

                    int SiteStart = exon.getStartPosition();

                    ArrayList<int[]> SiteCoverage = CoverageManager.getCoverageForSites(exon);

                    for (int pos = 0; pos < SiteCoverage.get(0).length; pos++) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(gene.getName()).append(",").append(exon.getChrStr()).append(",");
                        int total_coverage = SiteCoverage.get(0)[pos] + SiteCoverage.get(1)[pos];
                        sb.append(SiteStart + pos).append(",").append(total_coverage);
                        sb.append(",").append(SiteCoverage.get(0)[pos]);
                        sb.append(",").append(SiteCoverage.get(1)[pos]);
                        sb.append("\n");
                        bwSiteSummary.write(sb.toString());

                        //emit site info for potential processing
                        emitSiteInfo(gene.getName(), exon.getChrStr(), SiteStart + pos,
                                SiteCoverage.get(0)[pos], SiteCoverage.get(1)[pos]);
                    }
                }

                ss.updateSampleRegionCoverage(gene);
                ss.printGeneSummary(gene, bwCoverageSummaryByGene);
            }

            ss.print(bwSampleSummary);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputCleanedExonList() throws Exception {
        final String CleanedSiteList = CommonCommand.outputPath + "site.clean.txt";
        BufferedWriter bwSiteClean = new BufferedWriter(new FileWriter(CleanedSiteList));
        BufferedWriter bwGeneSummaryClean = new BufferedWriter(new FileWriter(CleanedGeneSummaryList));
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
        HashSet<String> CleanedList = ec.GetRegionCleanList(cutoff);

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
            String str = ec.GetCleanedGeneStringSite(gene, CleanedList);
            if (!str.isEmpty()) {
                bwSiteClean.write(str);
                bwSiteClean.newLine();
            }

            str = ec.GetCleanedGeneSummaryString(gene, CleanedList, true);
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

    private void emitSiteInfo(String gene, String chr, int position, int caseCoverage, int ctrlCoverage) {
        StringBuilder str = new StringBuilder();
        str.append(gene).append("_").append(chr).append("_").append(position);
        double caseAverage = ((double) caseCoverage) / SampleManager.getCaseNum();
        double ctrlAverage = ((double) ctrlCoverage) / SampleManager.getCtrlNum();
        double abs_diff = Math.abs(caseAverage - ctrlAverage);
        ec.AddRegionToList(str.toString(), caseAverage, ctrlAverage, abs_diff);
    }

    @Override
    public String toString() {
        return "It is running site coverage comparison function...";
    }

}
