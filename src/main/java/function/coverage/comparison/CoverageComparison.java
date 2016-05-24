package function.coverage.comparison;

import function.AnalysisBase;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.coverage.base.SampleStatistics;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import utils.FormatManager;

/**
 *
 * @author qwang, nick
 */
public class CoverageComparison extends AnalysisBase {

    public BufferedWriter bwSampleSummary = null;
    public BufferedWriter bwSampleRegionSummary = null;
    public BufferedWriter bwSampleMatrixSummary = null;
    public BufferedWriter bwSampleExonMatrixSummary = null;
    public BufferedWriter bwCoverageSummaryByExon = null;
    public BufferedWriter bwCoverageSummaryByGene = null;

    final String coverageSummaryByExon = CommonCommand.outputPath + "coverage.summary.by.exon.csv";
    final String coverageSummaryByGene = CommonCommand.outputPath + "coverage.summary.csv";
    final String CleanedExonList = CommonCommand.outputPath + "exon.clean.txt";
    final String CleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";
    final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    final String coverageDetailsFilePath = CommonCommand.outputPath + "coverage.details.csv";
    final String coverageMatrixFilePath = CommonCommand.outputPath + "coverage.details.matrix.csv";
    final String coverageExonMatrixFilePath = CommonCommand.outputPath + "coverage.details.matrix.by.exon.csv";

    @Override
    public void initOutput() {
        try {
            bwSampleSummary = new BufferedWriter(new FileWriter(sampleSummaryFilePath));
            bwSampleSummary.write("Sample,Total_Bases,Total_Covered_Base,%Overall_Bases_Covered,"
                    + "Total_Regions,Total_Covered_Regions,%Regions_Covered");
            bwSampleSummary.newLine();

            bwSampleRegionSummary = new BufferedWriter(new FileWriter(coverageDetailsFilePath));
            bwSampleRegionSummary.write("Sample,Gene/Transcript/Region,Chr,Length,"
                    + "Covered_Base,%Bases_Covered,Coverage_Status");
            bwSampleRegionSummary.newLine();

            bwSampleMatrixSummary = new BufferedWriter(new FileWriter(coverageMatrixFilePath));

            bwSampleExonMatrixSummary = new BufferedWriter(new FileWriter(coverageExonMatrixFilePath));

            bwCoverageSummaryByGene = new BufferedWriter(new FileWriter(coverageSummaryByGene));
            if (CoverageCommand.isCoverageComparisonDoLinear) {
                bwCoverageSummaryByGene.write("Gene,Chr,AvgAll,Length");
            } else {
                bwCoverageSummaryByGene.write("Gene,Chr,AvgCase,AvgCtrl,AbsDiff,Length,CoverageImbalanceWarning");
            }
            bwCoverageSummaryByGene.newLine();

            bwCoverageSummaryByExon = new BufferedWriter(new FileWriter(coverageSummaryByExon));
            if (CoverageCommand.isCoverageComparisonDoLinear) {
                bwCoverageSummaryByExon.write("EXON,Chr,AvgAll,pvalue,R2,Variance,Length");
            } else {
                bwCoverageSummaryByExon.write("EXON,Chr,AvgCase,AvgCtrl,AbsDiff,Length");
            }
            bwCoverageSummaryByExon.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwSampleSummary.flush();
            bwSampleSummary.close();
            bwSampleRegionSummary.flush();
            bwSampleRegionSummary.close();
            bwSampleMatrixSummary.flush();
            bwSampleMatrixSummary.close();
            bwSampleExonMatrixSummary.flush();
            bwSampleExonMatrixSummary.close();
            bwCoverageSummaryByGene.flush();
            bwCoverageSummaryByGene.close();
            bwCoverageSummaryByExon.flush();
            bwCoverageSummaryByExon.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (CoverageCommand.isCoverageComparisonDoLinear) {
            outputCleanedExonListLinearTrait();
        } else {
            outputCleanedExonList();
        }

        ThirdPartyToolManager.gzipFile(coverageDetailsFilePath);
        ThirdPartyToolManager.gzipFile(coverageMatrixFilePath);
        ThirdPartyToolManager.gzipFile(coverageExonMatrixFilePath);
    }

    @Override
    public void beforeProcessDatabaseData() {
        if (GenotypeLevelFilterCommand.minCoverage == Data.NO_FILTER) {
            ErrorManager.print("--min-coverage option has to be used in this function.");
        }

        int sampleSize = SampleManager.getListSize();
        if (!CoverageCommand.isCoverageComparisonDoLinear && (sampleSize == SampleManager.getCaseNum() || sampleSize == SampleManager.getCtrlNum())) {
            ErrorManager.print("Error: this function does not support to run with case only or control only sample file. ");
        }
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {
        try {
            SampleStatistics ss = new SampleStatistics(GeneManager.getGeneBoundaryList().size());
            ss.printMatrixHeader(bwSampleMatrixSummary, false);
            ss.printMatrixHeader(bwSampleExonMatrixSummary, true);

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                System.out.print("Processing " + (gene.getIndex() + 1) + " of "
                        + GeneManager.getGeneBoundaryList().size() + ": " + gene.toString() + "                              \r");

                for (Exon exon : gene.getExonList()) {
                    HashMap<Integer, Integer> result = CoverageManager.getCoverage(exon);
                    ss.accumulateCoverage(gene, result);
                    ss.printMatrixRowbyExon(result, gene, exon, bwSampleExonMatrixSummary);
                    
                    if (CoverageCommand.isCoverageComparisonDoLinear) {
                        ss.printExonSummaryLinearTrait(result, gene, exon, bwCoverageSummaryByExon);
                    } else {
                        ss.printExonSummary(result, gene, exon, bwCoverageSummaryByExon);
                    }
                }

                ss.print(gene, bwSampleRegionSummary);
                ss.printMatrixRow(gene, bwSampleMatrixSummary);

                if (CoverageCommand.isCoverageComparisonDoLinear) {
                    ss.printGeneSummaryLinearTrait(gene, bwCoverageSummaryByGene);
                } else {
                    ss.printGeneSummary(gene, bwCoverageSummaryByGene);
                }
            }

            ss.print(bwSampleSummary);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputCleanedExonListLinearTrait() {
        try {
            BufferedWriter bwExonClean = new BufferedWriter(new FileWriter(CleanedExonList)); //for now, will be much easier to test run
            BufferedWriter bwGeneSummaryClean = new BufferedWriter(new FileWriter(CleanedGeneSummaryList));
            bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgAll,CleanedLength");
            bwGeneSummaryClean.newLine();

            ExonCleanLinearTrait ec = new ExonCleanLinearTrait(coverageSummaryByExon);
            double cutoff = ec.GetCutoff();
            LogManager.writeAndPrint("\nThe automated cutoff value for variance for exons is " + Double.toString(cutoff));
            if (CoverageCommand.exonCleanCutoff >= 0) {
                cutoff = CoverageCommand.exonCleanCutoff;
                LogManager.writeAndPrint("User specified cutoff value " + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
            }
            HashSet<String> CleanedList = ec.GetExonCleanList(cutoff);

            int NumExonsTotal = ec.GetNumberOfExons();
            int NumExonsPruned = NumExonsTotal - CleanedList.size();

            LogManager.writeAndPrint("The number of exons before pruning is " + Integer.toString(NumExonsTotal));
            LogManager.writeAndPrint("The number of exons after pruning is " + Integer.toString(CleanedList.size()));
            LogManager.writeAndPrint("The number of exons pruned is " + Integer.toString(NumExonsPruned));
            double percentExonsPruned = (double) NumExonsPruned / (double) NumExonsTotal * 100;
            LogManager.writeAndPrint("The % of exons pruned is " + FormatManager.getSixDegitDouble(percentExonsPruned) + "%");

            LogManager.writeAndPrint("The total number of bases before pruning is " + FormatManager.getSixDegitDouble((double) ec.GetTotalBases() / 1000000.0) + " MB");
            LogManager.writeAndPrint("The total number of bases after pruning is " + FormatManager.getSixDegitDouble((double) ec.GetTotalCleanedBases() / 1000000.0) + " MB");
            LogManager.writeAndPrint("The % of bases pruned is " + FormatManager.getSixDegitDouble(100.0 - (double) ec.GetTotalCleanedBases() / (double) ec.GetTotalBases() * 100) + "%");

            LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  " + FormatManager.getSixDegitDouble(ec.GetAllCoverage() * 100) + "%");
            LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  " + FormatManager.getSixDegitDouble(ec.GetAllCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

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
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public void outputCleanedExonList() {
        try {
            BufferedWriter bwExonClean = new BufferedWriter(new FileWriter(CleanedExonList)); //for now, will be much easier to test run
            BufferedWriter bwGeneSummaryClean = new BufferedWriter(new FileWriter(CleanedGeneSummaryList));
            bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgCase,AvgCtrl,AbsDiff,CleanedLength,CoverageImbalanceWarning");
            bwGeneSummaryClean.newLine();

            RegionClean ec = new RegionClean(coverageSummaryByExon);
            double cutoff = ec.GetCutoff();
            LogManager.writeAndPrint("\nThe automated cutoff value for absolute mean coverage difference for exons is " + Double.toString(cutoff));
            if (CoverageCommand.exonCleanCutoff >= 0) {
                cutoff = CoverageCommand.exonCleanCutoff;
                LogManager.writeAndPrint("User specified cutoff value " + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
            }
            HashSet<String> CleanedList = ec.GetRegionCleanList(cutoff);
            int NumExonsTotal = ec.GetNumberOfRegions();

            int NumExonsPruned = NumExonsTotal - CleanedList.size();

            LogManager.writeAndPrint("The number of exons before pruning is " + Integer.toString(NumExonsTotal));
            LogManager.writeAndPrint("The number of exons after pruning is " + Integer.toString(CleanedList.size()));
            LogManager.writeAndPrint("The number of exons pruned is " + Integer.toString(NumExonsPruned));
            double percentExonsPruned = (double) NumExonsPruned / (double) NumExonsTotal * 100;
            LogManager.writeAndPrint("The % of exons pruned is " + FormatManager.getSixDegitDouble(percentExonsPruned) + "%");

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
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public String toString() {
        return "It is running coverage comparison function...";
    }
}
