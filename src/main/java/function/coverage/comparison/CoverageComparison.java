package function.coverage.comparison;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.coverage.base.ExonCleanLinearTrait;
import function.coverage.base.RegionClean;
import function.coverage.base.SampleStatistics;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.summary.CoverageSummary;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author qwang
 */
//this class can be potentially splitted into two classes of deriving one from another once the functionality is finallized.
public class CoverageComparison extends CoverageSummary {

    final String coverageSummaryByExon = CommonCommand.outputPath + "coverage.summary.by.exon.csv";
    final String coverageSummaryByGene = CommonCommand.outputPath + "coverage.summary.csv";
    final String CleanedExonList = CommonCommand.outputPath + "exon.clean.txt";
    final String CleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";

    public CoverageComparison() {
        super();
        int sampleSize = SampleManager.getListSize();
        if (!CoverageCommand.isCoverageComparisonDoLinear && (sampleSize == SampleManager.getCaseNum() || sampleSize == SampleManager.getCtrlNum())) {
            ErrorManager.print("Error: this function does not support to run with case only or control only sample file. ");
        }
    }

    public void outputCleanedExonListLinearTrait() throws Exception {
        NumberFormat pformat6 = new DecimalFormat("0.######");
        BufferedWriter bwExonClean = new BufferedWriter(new FileWriter(CleanedExonList)); //for now, will be much easier to test run
        BufferedWriter bwGeneSummaryClean = new BufferedWriter(new FileWriter(CleanedGeneSummaryList));
        bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgAll,CleanedLength");
        bwGeneSummaryClean.newLine();

        ExonCleanLinearTrait ec = new ExonCleanLinearTrait(coverageSummaryByExon);
        double cutoff = ec.GetCutoff();
        LogManager.writeAndPrint("\nThe automated cutoff value for variance for exons is " + Double.toString(cutoff));
        if (CoverageCommand.exonCleanCutoff >= 0) {
            cutoff = CoverageCommand.exonCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value " + pformat6.format(cutoff) + " is applied instead.");
        }
        HashSet<String> CleanedList = ec.GetExonCleanList(cutoff);

        int NumExonsTotal = ec.GetNumberOfExons();
        int NumExonsPruned = NumExonsTotal - CleanedList.size();

        LogManager.writeAndPrint("The number of exons before pruning is " + Integer.toString(NumExonsTotal));
        LogManager.writeAndPrint("The number of exons after pruning is " + Integer.toString(CleanedList.size()));
        LogManager.writeAndPrint("The number of exons pruned is " + Integer.toString(NumExonsPruned));
        double percentExonsPruned = (double) NumExonsPruned / (double) NumExonsTotal * 100;
        LogManager.writeAndPrint("The % of exons pruned is " + pformat6.format(percentExonsPruned) + "%");

        LogManager.writeAndPrint("The total number of bases before pruning is " + pformat6.format((double) ec.GetTotalBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is " + pformat6.format((double) ec.GetTotalCleanedBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is " + pformat6.format(100.0 - (double) ec.GetTotalCleanedBases() / (double) ec.GetTotalBases() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  " + pformat6.format(ec.GetAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  " + pformat6.format(ec.GetAllCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            String str = ec.getCleanedGeneString(gene, CleanedList);
            if (!str.isEmpty()) {
                bwExonClean.write(str);
                bwExonClean.newLine();
            }

            str = ec.GetCleanedGeneSummaryString(gene, CleanedList);
            if (!str.isEmpty()) {
                bwGeneSummaryClean.write(str);
                bwGeneSummaryClean.newLine();
            }
        }

        bwExonClean.flush();
        bwExonClean.close();
        bwGeneSummaryClean.flush();
        bwGeneSummaryClean.close();
    }

    public void outputCleanedExonList() throws Exception {
        NumberFormat pformat6 = new DecimalFormat("0.######");
        BufferedWriter bwExonClean = new BufferedWriter(new FileWriter(CleanedExonList)); //for now, will be much easier to test run
        BufferedWriter bwGeneSummaryClean = new BufferedWriter(new FileWriter(CleanedGeneSummaryList));
        bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgCase,AvgCtrl,AbsDiff,CleanedLength,CoverageImbalanceWarning");
        bwGeneSummaryClean.newLine();

        RegionClean ec = new RegionClean(coverageSummaryByExon);
        double cutoff = ec.GetCutoff();
        LogManager.writeAndPrint("\nThe automated cutoff value for absolute mean coverage difference for exons is " + Double.toString(cutoff));
        if (CoverageCommand.exonCleanCutoff >= 0) {
            cutoff = CoverageCommand.exonCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value " + pformat6.format(cutoff) + " is applied instead.");
        }
        HashSet<String> CleanedList = ec.GetRegionCleanList(cutoff);
        int NumExonsTotal = ec.GetNumberOfRegions();

        int NumExonsPruned = NumExonsTotal - CleanedList.size();

        LogManager.writeAndPrint("The number of exons before pruning is " + Integer.toString(NumExonsTotal));
        LogManager.writeAndPrint("The number of exons after pruning is " + Integer.toString(CleanedList.size()));
        LogManager.writeAndPrint("The number of exons pruned is " + Integer.toString(NumExonsPruned));
        double percentExonsPruned = (double) NumExonsPruned / (double) NumExonsTotal * 100;
        LogManager.writeAndPrint("The % of exons pruned is " + pformat6.format(percentExonsPruned) + "%");

        LogManager.writeAndPrint("The total number of bases before pruning is " + pformat6.format((double) ec.GetTotalBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is " + pformat6.format((double) ec.GetTotalCleanedBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is " + pformat6.format(100.0 - (double) ec.GetTotalCleanedBases() / (double) ec.GetTotalBases() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  " + pformat6.format(ec.GetAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  " + pformat6.format(ec.GetAllCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for cases after pruning is  " + pformat6.format(ec.GetCaseCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for cases after pruning is  " + pformat6.format(ec.GetCaseCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for controls after pruning is  " + pformat6.format(ec.GetControlCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for controls after pruning is  " + pformat6.format(ec.GetControlCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            String str = ec.GetCleanedGeneString(gene, CleanedList);
            if (!str.isEmpty()) {
                bwExonClean.write(str);
                bwExonClean.newLine();
            }

            str = ec.GetCleanedGeneSummaryString(gene, CleanedList, false);
            if (!str.isEmpty()) {
                bwGeneSummaryClean.write(str);
                bwGeneSummaryClean.newLine();
            }
        }
        
        bwExonClean.flush();
        bwExonClean.close();
        bwGeneSummaryClean.flush();
        bwGeneSummaryClean.close();
    }

    @Override
    public void DoExonSummary(SampleStatistics ss, int record, HashMap<Integer, Integer> result, Exon e) throws Exception {
        if (CoverageCommand.isCoverageComparisonDoLinear) {
            ss.printExonSummaryLinearTrait(record, result, e, bwCoverageSummaryByExon);
        } else {
            ss.printExonSummary(record, result, e, bwCoverageSummaryByExon);
        }
    }

    @Override
    public void DoGeneSummary(SampleStatistics ss, int record) throws Exception {
        if (CoverageCommand.isCoverageComparisonDoLinear) {
            ss.printGeneSummaryLinearTrait(record, bwCoverageSummaryByGene);
        } else {
            ss.printGeneSummary(record, bwCoverageSummaryByGene);
        }
    }

    @Override
    public void run() throws Exception {
        super.run();
        if (CoverageCommand.isByExon) {
            if (CoverageCommand.isCoverageComparisonDoLinear) {
                outputCleanedExonListLinearTrait();
            } else {
                outputCleanedExonList();
            }
        }
    }

    @Override
    public void initOutput() throws Exception {
        super.initOutput();
        bwCoverageSummaryByGene = new BufferedWriter(new FileWriter(coverageSummaryByGene));
        if (CoverageCommand.isCoverageComparisonDoLinear) {
            bwCoverageSummaryByGene.write("Gene,Chr,AvgAll,Length");
        } else {
            bwCoverageSummaryByGene.write("Gene,Chr,AvgCase,AvgCtrl,AbsDiff,Length,CoverageImbalanceWarning");
        }
        bwCoverageSummaryByGene.newLine();
        if (CoverageCommand.isByExon) {
            bwCoverageSummaryByExon = new BufferedWriter(new FileWriter(coverageSummaryByExon));
            if (CoverageCommand.isCoverageComparisonDoLinear) {
                bwCoverageSummaryByExon.write("EXON,Chr,AvgAll,pvalue,R2,Variance,Length");
            } else {
                bwCoverageSummaryByExon.write("EXON,Chr,AvgCase,AvgCtrl,AbsDiff,Length");
            }
            bwCoverageSummaryByExon.newLine();
        }
    }

    @Override
    public void closeOutput() throws Exception {
        super.closeOutput();
        bwCoverageSummaryByGene.flush();
        bwCoverageSummaryByGene.close();
        if (CoverageCommand.isByExon) {
            bwCoverageSummaryByExon.flush();
            bwCoverageSummaryByExon.close();
        }

        ThirdPartyToolManager.gzipFile(coverageDetailsFilePath);
        ThirdPartyToolManager.gzipFile(coverageMatrixFilePath);
        ThirdPartyToolManager.gzipFile(coverageExonMatrixFilePath);
    }
}
