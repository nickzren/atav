package function.coverage.comparison;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
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
public class CoverageComparison extends CoverageComparisonBase {

    public BufferedWriter bwCoverageSummaryByExon = null;

    final String coverageSummaryByExon = CommonCommand.outputPath + "coverage.summary.by.exon.csv";

    final String cleanedExonList = CommonCommand.outputPath + "exon.clean.txt";
    final String cleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";

    @Override
    public void initOutput() {
        try {
            super.initOutput();

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
            super.closeOutput();

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
    }

    @Override
    public void processExon(HashMap<Integer, Integer> result, Gene gene, Exon exon) {
        if (CoverageCommand.isCoverageComparisonDoLinear) {
            outputExonSummaryLinearTrait(result, gene, exon);
        } else {
            outputExonSummary(result, gene, exon);
        }
    }

    private void outputExonSummaryLinearTrait(HashMap<Integer, Integer> result, Gene gene, Exon exon) {
        try {
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
            bwCoverageSummaryByExon.write(sb.toString());
            bwCoverageSummaryByExon.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputExonSummary(HashMap<Integer, Integer> result, Gene gene, Exon exon) {
        try {
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
            bwCoverageSummaryByExon.write(sb.toString());
            bwCoverageSummaryByExon.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputCleanedExonListLinearTrait() {
        try {
            BufferedWriter bwExonClean = new BufferedWriter(new FileWriter(cleanedExonList)); //for now, will be much easier to test run
            BufferedWriter bwGeneSummaryClean = new BufferedWriter(new FileWriter(cleanedGeneSummaryList));
            bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgAll,CleanedLength");
            bwGeneSummaryClean.newLine();

            ExonCleanLinearTrait ec = new ExonCleanLinearTrait(coverageSummaryByExon);
            double cutoff = ec.GetCutoff();
            LogManager.writeAndPrint("\nThe automated cutoff value for variance for exons is " + Double.toString(cutoff));
            if (CoverageCommand.exonCleanCutoff >= 0) {
                cutoff = CoverageCommand.exonCleanCutoff;
                LogManager.writeAndPrint("User specified cutoff value " + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
            }
            HashSet<String> cleanedList = ec.getExonCleanList(cutoff);

            int numExonsTotal = ec.getNumberOfExons();
            int numExonsPruned = numExonsTotal - cleanedList.size();

            LogManager.writeAndPrint("The number of exons before pruning is " + Integer.toString(numExonsTotal));
            LogManager.writeAndPrint("The number of exons after pruning is " + Integer.toString(cleanedList.size()));
            LogManager.writeAndPrint("The number of exons pruned is " + Integer.toString(numExonsPruned));
            double percentExonsPruned = (double) numExonsPruned / (double) numExonsTotal * 100;
            LogManager.writeAndPrint("The % of exons pruned is " + FormatManager.getSixDegitDouble(percentExonsPruned) + "%");

            LogManager.writeAndPrint("The total number of bases before pruning is " + FormatManager.getSixDegitDouble((double) ec.GetTotalBases() / 1000000.0) + " MB");
            LogManager.writeAndPrint("The total number of bases after pruning is " + FormatManager.getSixDegitDouble((double) ec.GetTotalCleanedBases() / 1000000.0) + " MB");
            LogManager.writeAndPrint("The % of bases pruned is " + FormatManager.getSixDegitDouble(100.0 - (double) ec.GetTotalCleanedBases() / (double) ec.GetTotalBases() * 100) + "%");

            LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  " + FormatManager.getSixDegitDouble(ec.GetAllCoverage() * 100) + "%");
            LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  " + FormatManager.getSixDegitDouble(ec.GetAllCoverage() * ec.GetTotalBases() / 1000000.0) + " MB");

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                String str = ec.getCleanedGeneString(gene, cleanedList);
                if (!str.isEmpty()) {
                    bwExonClean.write(str);
                    bwExonClean.newLine();
                }

                str = ec.GetCleanedGeneSummaryString(gene, cleanedList);
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

    private void outputCleanedExonList() {
        try {
            BufferedWriter bwExonClean = new BufferedWriter(new FileWriter(cleanedExonList)); //for now, will be much easier to test run
            BufferedWriter bwGeneSummaryClean = new BufferedWriter(new FileWriter(cleanedGeneSummaryList));
            bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgCase,AvgCtrl,AbsDiff,CleanedLength,CoverageImbalanceWarning");
            bwGeneSummaryClean.newLine();

            RegionClean ec = new RegionClean(coverageSummaryByExon);
            double cutoff = ec.GetCutoff();
            LogManager.writeAndPrint("\nThe automated cutoff value for absolute mean coverage difference for exons is " + Double.toString(cutoff));
            if (CoverageCommand.exonCleanCutoff >= 0) {
                cutoff = CoverageCommand.exonCleanCutoff;
                LogManager.writeAndPrint("User specified cutoff value " + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
            }
            HashSet<String> cleanedList = ec.getRegionCleanList(cutoff);
            int numExonsTotal = ec.getNumberOfRegions();

            int numExonsPruned = numExonsTotal - cleanedList.size();

            LogManager.writeAndPrint("The number of exons before pruning is " + Integer.toString(numExonsTotal));
            LogManager.writeAndPrint("The number of exons after pruning is " + Integer.toString(cleanedList.size()));
            LogManager.writeAndPrint("The number of exons pruned is " + Integer.toString(numExonsPruned));
            double percentExonsPruned = (double) numExonsPruned / (double) numExonsTotal * 100;
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
                String str = ec.GetCleanedGeneString(gene, cleanedList);
                if (!str.isEmpty()) {
                    bwExonClean.write(str);
                    bwExonClean.newLine();
                }

                str = ec.getCleanedGeneSummaryString(gene, cleanedList, false);
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
