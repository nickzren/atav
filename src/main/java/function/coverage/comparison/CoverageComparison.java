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
import java.util.Set;
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
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void afterProcessDatabaseData() {
        try {
            // needs to close , in order to initialize values within below functions
            bwCoverageSummaryByExon.flush();
            bwCoverageSummaryByExon.close();

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
            Set<Integer> samples = sampleCoveredLengthMap.keySet();
            double regoinLength = exon.getLength();
            double avgAll = 0;
            SimpleRegression sr = new SimpleRegression(true);
            SummaryStatistics lss = new SummaryStatistics();
            for (Sample sample : SampleManager.getList()) {
                double coveredLength = 0;
                if (samples.contains(sample.getId())) {
                    coveredLength = sampleCoveredLengthMap.get(sample.getId());
                }
                avgAll = avgAll + coveredLength;
                double x = sample.getQuantitativeTrait();
                double y = MathManager.devide(coveredLength, regoinLength);
                sr.addData(x, y);
                lss.addValue(y);
            }

            avgAll = MathManager.devide(avgAll, SampleManager.getListSize());
            avgAll = MathManager.devide(avgAll, regoinLength);

            double R2 = sr.getRSquare();
            double pValue = sr.getSignificance();
            double Variance = lss.getVariance();

            StringBuilder sb = new StringBuilder();
            sb.append(gene.getName()).append("_").append(exon.getIdStr()).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(FormatManager.getSixDegitDouble(avgAll)).append(",");
            if (Double.isNaN(pValue)) { //happens if all coverages are the same
                sb.append(1).append(",");     //do not format here as we need to reuse it for precision
                sb.append(0).append(",");
            } else {
                sb.append(pValue).append(","); //do not format here as we need to reuse it for precision
                sb.append(R2 * 100).append(",");
            }
            sb.append(Variance).append(",");
            sb.append(exon.getLength());
            bwCoverageSummaryByExon.write(sb.toString());
            bwCoverageSummaryByExon.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputExonSummary(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        try {
            Set<Integer> samples = sampleCoveredLengthMap.keySet();

            double avgCase = 0;
            double avgCtrl = 0;
            for (Sample sample : SampleManager.getList()) {
                int coveredLength = 0;
                if (samples.contains(sample.getId())) {
                    coveredLength = sampleCoveredLengthMap.get(sample.getId());
                }
                
                if (sample.isCase()) {
                    avgCase = avgCase + coveredLength;
                } else {
                    avgCtrl = avgCtrl + coveredLength;
                }
            }
            
            avgCase = MathManager.devide(avgCase, SampleManager.getCaseNum());
            avgCase = MathManager.devide(avgCase, exon.getLength());
            avgCtrl = MathManager.devide(avgCtrl, SampleManager.getCtrlNum());
            avgCtrl = MathManager.devide(avgCtrl, exon.getLength());

            StringBuilder sb = new StringBuilder();
            sb.append(gene.getName()).append("_").append(exon.getIdStr()).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(FormatManager.getSixDegitDouble(avgCase)).append(",");
            sb.append(FormatManager.getSixDegitDouble(avgCtrl)).append(",");
            sb.append(FormatManager.getSixDegitDouble(MathManager.abs(avgCase, avgCtrl))).append(",");
            sb.append(exon.getLength());
            bwCoverageSummaryByExon.write(sb.toString());
            bwCoverageSummaryByExon.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputCleanedExonListLinearTrait() {
        try {
            RegionCleanLinear regionCleanLinear = new RegionCleanLinear(coverageSummaryByExon);

            regionCleanLinear.initSortedExonSet();

            printCleanedExonListLinearTraitLog(regionCleanLinear);

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                String str = regionCleanLinear.getCleanedGeneString(gene);
                if (!str.isEmpty()) {
                    bwExonClean.write(str);
                    bwExonClean.newLine();
                }

                str = regionCleanLinear.getCleanedGeneSummaryStrByExon(gene);
                if (!str.isEmpty()) {
                    bwGeneSummaryClean.write(str);
                    bwGeneSummaryClean.newLine();
                }
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void printCleanedExonListLinearTraitLog(RegionCleanLinear regionCleanLinear) {
        int numExonsTotal = regionCleanLinear.getSortedExonListSize();
        int numExonsPruned = numExonsTotal - regionCleanLinear.getSortedExonMapSize();

        LogManager.writeAndPrint("The number of exons before pruning is "
                + Integer.toString(numExonsTotal));
        LogManager.writeAndPrint("The number of exons after pruning is "
                + Integer.toString(regionCleanLinear.getSortedExonMapSize()));
        LogManager.writeAndPrint("The number of exons pruned is "
                + Integer.toString(numExonsPruned));
        double percentExonsPruned = (double) numExonsPruned / (double) numExonsTotal * 100;
        LogManager.writeAndPrint("The % of exons pruned is "
                + FormatManager.getSixDegitDouble(percentExonsPruned) + "%");

        LogManager.writeAndPrint("The total number of bases before pruning is "
                + FormatManager.getSixDegitDouble((double) regionCleanLinear.getTotalBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is "
                + FormatManager.getSixDegitDouble((double) regionCleanLinear.getTotalCleanedBases() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is "
                + FormatManager.getSixDegitDouble(100.0 - (double) regionCleanLinear.getTotalCleanedBases() / (double) regionCleanLinear.getTotalBases() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  "
                + FormatManager.getSixDegitDouble(regionCleanLinear.getAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  "
                + FormatManager.getSixDegitDouble(regionCleanLinear.getAllCoverage() * regionCleanLinear.getTotalBases() / 1000000.0) + " MB");
    }

    private void outputCleanedExonList() {
        try {
            RegionClean regionClean = new RegionClean(coverageSummaryByExon);

            regionClean.initSortedRegionMap();

            printCleanedExonListLog(regionClean);

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                String str = regionClean.getCleanedGeneStrByExon(gene);
                if (!str.isEmpty()) {
                    bwExonClean.write(str);
                    bwExonClean.newLine();
                }

                str = regionClean.getCleanedGeneSummaryStrByExon(gene);
                if (!str.isEmpty()) {
                    bwGeneSummaryClean.write(str);
                    bwGeneSummaryClean.newLine();
                }
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void printCleanedExonListLog(RegionClean regionClean) {
        int numExonsTotal = regionClean.getSortedRegionListSite();
        int numExonsPruned = numExonsTotal - regionClean.getSortedRegionMapSize();
        LogManager.writeAndPrint("The number of exons before pruning is "
                + Integer.toString(numExonsTotal));
        LogManager.writeAndPrint("The number of exons after pruning is "
                + Integer.toString(regionClean.getSortedRegionMapSize()));
        LogManager.writeAndPrint("The number of exons pruned is "
                + Integer.toString(numExonsPruned));
        double percentExonsPruned = (double) numExonsPruned / (double) numExonsTotal * 100;
        LogManager.writeAndPrint("The % of exons pruned is "
                + FormatManager.getSixDegitDouble(percentExonsPruned) + "%");

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
        return "It is running coverage comparison function...";
    }
}
