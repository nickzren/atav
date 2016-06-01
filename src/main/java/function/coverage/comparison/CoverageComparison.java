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
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author qwang, nick
 */
public class CoverageComparison extends CoverageComparisonBase {

    BufferedWriter bwCoverageSummaryByExon = null;
    BufferedWriter bwExonClean = null;
    BufferedWriter bwGeneSummaryClean = null;

    final String coverageSummaryByExon = CommonCommand.outputPath + "coverage.summary.by.exon.csv";
    final String cleanedExonList = CommonCommand.outputPath + "exon.clean.txt";
    final String cleanedGeneSummaryList = CommonCommand.outputPath + "coverage.summary.clean.csv";

    ExonClean exonClean = new ExonClean();
    ExonCleanLinear exonCleanLinear = new ExonCleanLinear();

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

            bwExonClean = new BufferedWriter(new FileWriter(cleanedExonList));
            
            bwGeneSummaryClean = new BufferedWriter(new FileWriter(cleanedGeneSummaryList));
            if (CoverageCommand.isCoverageComparisonDoLinear) {
                bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgAll,CleanedLength");
            } else {
                bwGeneSummaryClean.write("Gene,Chr,OriginalLength,AvgCase,AvgCtrl,AbsDiff,CleanedLength,CoverageImbalanceWarning");
            }
            bwGeneSummaryClean.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            super.closeOutput();

            bwExonClean.flush();
            bwExonClean.close();
            bwGeneSummaryClean.flush();
            bwGeneSummaryClean.close();
            bwCoverageSummaryByExon.flush();
            bwCoverageSummaryByExon.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void afterProcessDatabaseData() {
        try {
            if (CoverageCommand.isCoverageComparisonDoLinear) {
                outputCleanedExonListLinearTrait();
            } else {
                outputCleanedExonList();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void processExon(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        if (CoverageCommand.isCoverageComparisonDoLinear) {
            outputExonSummaryLinearTrait(sampleCoveredLengthMap, gene, exon);
        } else {
            outputExonSummary(sampleCoveredLengthMap, gene, exon);
        }
    }

    private void outputExonSummaryLinearTrait(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        try {
            double regoinLength = exon.getLength();
            double avgAll = 0;
            SimpleRegression sr = new SimpleRegression(true);
            SummaryStatistics lss = new SummaryStatistics();
            for (Sample sample : SampleManager.getList()) {
                Integer coveredLength = sampleCoveredLengthMap.get(sample.getId());
                if (coveredLength == null) {
                    coveredLength = 0;
                }
                avgAll = avgAll + coveredLength;
                double x = sample.getQuantitativeTrait();
                double y = MathManager.devide(coveredLength, regoinLength);
                sr.addData(x, y);
                lss.addValue(y);
            }

            avgAll = MathManager.devide(avgAll, SampleManager.getListSize());
            avgAll = MathManager.devide(avgAll, regoinLength);

            double r2 = sr.getRSquare();
            double pValue = sr.getSignificance();
            double variance = lss.getVariance();

            StringBuilder sb = new StringBuilder();
            String name = gene.getName() + "_" + exon.getIdStr();
            sb.append(name).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(FormatManager.getSixDegitDouble(avgAll)).append(",");
            
            if (Double.isNaN(pValue)) { //happens if all coverages are the same
                pValue = 1;
                r2 = 0;
            } else {
                r2 *= 100;
            }
            sb.append(pValue).append(",");
            sb.append(r2).append(",");
            sb.append(variance).append(",");
            sb.append(exon.getLength());
            bwCoverageSummaryByExon.write(sb.toString());
            bwCoverageSummaryByExon.newLine();

            exonCleanLinear.addExon(name, avgAll, pValue, r2, variance, exon.getLength());
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputExonSummary(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        try {
            double caseAvg = 0;
            double ctrlAvg = 0;
            for (Sample sample : SampleManager.getList()) {
                Integer coveredLength = sampleCoveredLengthMap.get(sample.getId());

                if (coveredLength != null) {
                    if (sample.isCase()) {
                        caseAvg += coveredLength;
                    } else {
                        ctrlAvg += coveredLength;
                    }
                }
            }

            caseAvg = MathManager.devide(caseAvg, SampleManager.getCaseNum());
            caseAvg = MathManager.devide(caseAvg, exon.getLength());
            ctrlAvg = MathManager.devide(ctrlAvg, SampleManager.getCtrlNum());
            ctrlAvg = MathManager.devide(ctrlAvg, exon.getLength());

            StringBuilder sb = new StringBuilder();
            String name = gene.getName() + "_" + exon.getIdStr();
            sb.append(name).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(FormatManager.getSixDegitDouble(caseAvg)).append(",");
            sb.append(FormatManager.getSixDegitDouble(ctrlAvg)).append(",");
            double absDiff = MathManager.abs(caseAvg, ctrlAvg);
            sb.append(FormatManager.getSixDegitDouble(absDiff)).append(",");
            sb.append(exon.getLength());
            bwCoverageSummaryByExon.write(sb.toString());
            bwCoverageSummaryByExon.newLine();

            exonClean.addExon(name, caseAvg, ctrlAvg, absDiff, exon.getLength());
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputCleanedExonListLinearTrait() {
        try {
            exonCleanLinear.initCleanedExonMap();

            printCleanedExonListLinearTraitLog();

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                String str = exonCleanLinear.getCleanedGeneStrByExon(gene);
                if (!str.isEmpty()) {
                    bwExonClean.write(str);
                    bwExonClean.newLine();
                }

                str = exonCleanLinear.getCleanedGeneSummaryStrByExon(gene);
                if (!str.isEmpty()) {
                    bwGeneSummaryClean.write(str);
                    bwGeneSummaryClean.newLine();
                }
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void printCleanedExonListLinearTraitLog() {
        int numExonsTotal = exonCleanLinear.getExonListSize();
        int numExonsPruned = numExonsTotal - exonCleanLinear.getCleanedExonMapSize();

        LogManager.writeAndPrint("The number of exons before pruning is "
                + Integer.toString(numExonsTotal));
        LogManager.writeAndPrint("The number of exons after pruning is "
                + Integer.toString(exonCleanLinear.getCleanedExonMapSize()));
        LogManager.writeAndPrint("The number of exons pruned is "
                + Integer.toString(numExonsPruned));
        double percentExonsPruned = (double) numExonsPruned / (double) numExonsTotal * 100;
        LogManager.writeAndPrint("The % of exons pruned is "
                + FormatManager.getSixDegitDouble(percentExonsPruned) + "%");

        LogManager.writeAndPrint("The total number of bases before pruning is "
                + FormatManager.getSixDegitDouble((double) exonCleanLinear.getTotalBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is "
                + FormatManager.getSixDegitDouble((double) exonCleanLinear.getTotalCleanedBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is "
                + FormatManager.getSixDegitDouble(100.0 - (double) exonCleanLinear.getTotalCleanedBases() / (double) exonCleanLinear.getTotalBases() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  "
                + FormatManager.getSixDegitDouble(exonCleanLinear.getAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  "
                + FormatManager.getSixDegitDouble(exonCleanLinear.getAllCoverage() * exonCleanLinear.getTotalBases() / 1000000.0) + " MB");
    }

    private void outputCleanedExonList() {
        try {
            exonClean.initCleanedExonMap();

            printCleanedExonListLog();

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                String str = exonClean.getCleanedGeneStrByExon(gene);
                if (!str.isEmpty()) {
                    bwExonClean.write(str);
                    bwExonClean.newLine();
                }

                str = exonClean.getCleanedGeneSummaryStrByExon(gene);
                if (!str.isEmpty()) {
                    bwGeneSummaryClean.write(str);
                    bwGeneSummaryClean.newLine();
                }
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void printCleanedExonListLog() {
        int numExonsTotal = exonClean.getExonListSite();
        int numExonsPruned = numExonsTotal - exonClean.getCleanedExonMapSize();
        LogManager.writeAndPrint("The number of exons before pruning is "
                + Integer.toString(numExonsTotal));
        LogManager.writeAndPrint("The number of exons after pruning is "
                + Integer.toString(exonClean.getCleanedExonMapSize()));
        LogManager.writeAndPrint("The number of exons pruned is "
                + Integer.toString(numExonsPruned));
        double percentExonsPruned = (double) numExonsPruned / (double) numExonsTotal * 100;
        LogManager.writeAndPrint("The % of exons pruned is "
                + FormatManager.getSixDegitDouble(percentExonsPruned) + "%");

        LogManager.writeAndPrint("The total number of bases before pruning is "
                + FormatManager.getSixDegitDouble((double) exonClean.getTotalBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is "
                + FormatManager.getSixDegitDouble((double) exonClean.getTotalCleanedBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is "
                + FormatManager.getSixDegitDouble(100.0 - (double) exonClean.getTotalCleanedBases() / (double) exonClean.getTotalBases() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  "
                + FormatManager.getSixDegitDouble(exonClean.getAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  "
                + FormatManager.getSixDegitDouble(exonClean.getAllCoverage() * exonClean.getTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for cases after pruning is  "
                + FormatManager.getSixDegitDouble(exonClean.getCaseCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for cases after pruning is  "
                + FormatManager.getSixDegitDouble(exonClean.getCaseCoverage() * exonClean.getTotalBases() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for controls after pruning is  "
                + FormatManager.getSixDegitDouble(exonClean.getCtrlCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for controls after pruning is  "
                + FormatManager.getSixDegitDouble(exonClean.getCtrlCoverage() * exonClean.getTotalBases() / 1000000.0) + " MB");
    }

    @Override
    public String toString() {
        return "It is running coverage comparison function...";
    }
}
