package function.coverage.comparison;

import function.AnalysisBase;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.coverage.base.SampleStatistics;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.Sample;
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
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import utils.FormatManager;

/**
 *
 * @author qwang, nick
 */
public class CoverageComparison extends AnalysisBase {

    public BufferedWriter bwSampleSummary = null;
    public BufferedWriter bwSampleRegionSummary = null;
    public BufferedWriter bwCoverageSummaryByExon = null;
    public BufferedWriter bwCoverageSummaryByGene = null;

    final String coverageSummaryByExon = CommonCommand.outputPath + "coverage.summary.by.exon.csv";
    final String coverageSummaryByGene = CommonCommand.outputPath + "coverage.summary.csv";
    final String CleanedExonList = CommonCommand.outputPath + "exon.clean.txt";
    final String CleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";
    final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    final String coverageDetailsFilePath = CommonCommand.outputPath + "coverage.details.csv";

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

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                System.out.print("Processing " + (gene.getIndex() + 1) + " of "
                        + GeneManager.getGeneBoundaryList().size() + ": " + gene.toString() + "                              \r");

                for (Exon exon : gene.getExonList()) {
                    HashMap<Integer, Integer> result = CoverageManager.getCoverage(exon);
                    ss.accumulateCoverage(gene, result);

                    if (CoverageCommand.isCoverageComparisonDoLinear) {
                        outputExonSummaryLinearTrait(result, gene, exon);
                    } else {
                        outputExonSummary(result, gene, exon);
                    }
                }

                ss.print(gene, bwSampleRegionSummary);

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

    public void outputExonSummaryLinearTrait(HashMap<Integer, Integer> result, Gene gene, Exon exon) throws Exception {
        Set<Integer> samples = result.keySet();
        double RegoinLength = exon.getLength();
        double avgAll = 0;
        SimpleRegression sr = new SimpleRegression(true);
        SummaryStatistics lss = new SummaryStatistics();
        for (Sample sample : SampleManager.getList()) {
            double cov = 0;
            if (samples.contains(sample.getId())) {
                cov = result.get(sample.getId());
            }
            avgAll = avgAll + cov;
            double x = sample.getQuantitativeTrait();
            double y = cov / RegoinLength;
            sr.addData(x, y);
            lss.addValue(y);
        }
        avgAll = avgAll / SampleManager.getListSize() / RegoinLength;
        double R2 = sr.getRSquare();
        double pValue = sr.getSignificance();
        double Variance = lss.getVariance();

        StringBuilder sb = new StringBuilder();
        sb.append(gene.getName()).append("_").append(exon.getIdStr());
        sb.append(",").append(gene.getChr());
        sb.append(",").append(FormatManager.getSixDegitDouble(avgAll));
        if (Double.isNaN(pValue)) { //happens if all coverages are the same
            sb.append(",").append(1);     //do not format here as we need to reuse it for precision
            sb.append(",").append(0);
        } else {
            sb.append(",").append(pValue); //do not format here as we need to reuse it for precision
            sb.append(",").append(R2 * 100);
        }
        sb.append(",").append(Variance);

        sb.append(",").append(exon.getLength());
        sb.append("\n");
        bwCoverageSummaryByExon.write(sb.toString());
    }

    public void outputExonSummary(HashMap<Integer, Integer> result, Gene gene, Exon exon) throws Exception {
        if (SampleManager.getCaseNum() == 0 || SampleManager.getCtrlNum() == 0) {
            return;
        }

        Set<Integer> samples = result.keySet();

        double avgCase = 0;
        double avgCtrl = 0;
        for (Sample sample : SampleManager.getList()) {
            int cov = 0;
            if (samples.contains(sample.getId())) {
                cov = result.get(sample.getId());

            }
            if (sample.isCase()) {
                avgCase = avgCase + cov;
            } else {
                avgCtrl = avgCtrl + cov;
            }
        }
        avgCase = avgCase / SampleManager.getCaseNum() / exon.getLength();
        avgCtrl = avgCtrl / SampleManager.getCtrlNum() / exon.getLength();

        StringBuilder sb = new StringBuilder();
        sb.append(gene.getName()).append("_").append(exon.getIdStr());
        sb.append(",").append(gene.getChr());
        sb.append(",").append(FormatManager.getSixDegitDouble(avgCase));
        sb.append(",").append(FormatManager.getSixDegitDouble(avgCtrl));
        sb.append(",").append(FormatManager.getSixDegitDouble(Math.abs((avgCase - avgCtrl))));
        sb.append(",").append(exon.getLength());
        sb.append("\n");
        bwCoverageSummaryByExon.write(sb.toString());
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
