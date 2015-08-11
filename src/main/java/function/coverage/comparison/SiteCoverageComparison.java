package function.coverage.comparison;

import function.coverage.base.CoverageCommand;
import function.coverage.base.RegionClean;
import function.coverage.base.Gene;
import function.coverage.summary.SiteCoverageSummary;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author qwang
 */

public class SiteCoverageComparison extends SiteCoverageSummary {

    final String CleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";
    public BufferedWriter bwCoverageSummaryByGene = null;
    private RegionClean ec = new RegionClean();

    public SiteCoverageComparison() {
        super();
        int sampleSize = SampleManager.getListSize();
        if (sampleSize == SampleManager.getCaseNum() || sampleSize == SampleManager.getCtrlNum()) {
            ErrorManager.print("Error: this function is not supposed to run with case only or control only sample file. ");
        }
    }

    @Override
    public void emitSiteInfo(String gene, String chr, int position, int caseCoverage, int ctrlCoverage) {
        StringBuilder str = new StringBuilder();
        str.append(gene).append("_").append(chr).append("_").append(position);
        double caseAverage = ((double)caseCoverage) / SampleManager.getCaseNum();
        double ctrlAverage = ((double)ctrlCoverage) / SampleManager.getCtrlNum();
        double abs_diff = Math.abs(caseAverage - ctrlAverage);
        ec.AddRegionToList(str.toString(), caseAverage, ctrlAverage, abs_diff);
    }
    
    public void outputCleanedExonList() throws Exception {
        NumberFormat pformat6 = new DecimalFormat("0.######");
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
            LogManager.writeAndPrint("User specified cutoff value " + pformat6.format(cutoff) + " is applied instead.");
        }
        HashSet<String> CleanedList = ec.GetRegionCleanList(cutoff);
               
        LogManager.writeAndPrint("The total number of bases before pruning is " + pformat6.format((double) ec.GetTotalBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is " + pformat6.format((double) ec.GetTotalCleanedBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is " + pformat6.format(100.0 - (double) ec.GetTotalCleanedBases() / (double) ec.GetTotalBases() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  " + pformat6.format(ec.GetAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  " + pformat6.format(ec.GetAllCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for cases after pruning is  " + pformat6.format(ec.GetCaseCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for cases after pruning is  " + pformat6.format(ec.GetCaseCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for controls after pruning is  " + pformat6.format(ec.GetControlCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for controls after pruning is  " + pformat6.format(ec.GetControlCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        for (Iterator it = this.iterator(); it.hasNext();) {
            Object obj = it.next();
            String JobType = obj.getClass().getSimpleName();
            if (JobType.equals("Gene")) {
                Gene gene = (Gene) obj;
                String str =ec.GetCleanedGeneStringSite(gene, CleanedList);
                if (!str.isEmpty()) {
                    bwSiteClean.write(str);
                    bwSiteClean.newLine();
                }
                
                str = ec.GetCleanedGeneSummaryString(gene, CleanedList,true);
                if (!str.isEmpty()) {
                    bwGeneSummaryClean.write(str);
                    bwGeneSummaryClean.newLine();
                }
            }
        }
        bwSiteClean.flush();
        bwSiteClean.close();
        bwGeneSummaryClean.flush();
        bwGeneSummaryClean.close();
    }
    
    @Override
    public void run() throws Exception {
        super.run();
        outputCleanedExonList();
    } 
}