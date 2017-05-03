package function.coverage.comparison;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
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

    final String coverageSummaryByExon = CommonCommand.outputPath + "coverage.summary.by.exon.csv";
    final String cleanedExonList = CommonCommand.outputPath + "exon.clean.txt";

    ExonClean regionClean = new ExonClean();
    ExonCleanLinear exonCleanLinear = new ExonCleanLinear();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwCoverageSummaryByExon = new BufferedWriter(new FileWriter(coverageSummaryByExon));
            bwCoverageSummaryByExon.write("EXON,Chr,Start,End,AvgCase,AvgCtrl,CovDiff,Length");
            if (CoverageCommand.isLinear) {
                bwCoverageSummaryByExon.write(",P Value,R2,Variance");
            }
            bwCoverageSummaryByExon.newLine();

            bwExonClean = new BufferedWriter(new FileWriter(cleanedExonList));
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
            bwCoverageSummaryByExon.flush();
            bwCoverageSummaryByExon.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void afterProcessDatabaseData() {
        try {
            if (CoverageCommand.isLinear) {
                outputCleanedExonListLinear();
            } else {
                outputCleanedExonList();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void processExon(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        try {
            SimpleRegression sr = new SimpleRegression(true);
            SummaryStatistics lss = new SummaryStatistics();

            float caseAvg = 0;
            float ctrlAvg = 0;
            for (Sample sample : SampleManager.getList()) {
                Integer coveredLength = sampleCoveredLengthMap.get(sample.getId());
                if (coveredLength != null) {
                    if (sample.isCase()) {
                        caseAvg += coveredLength;
                    } else {
                        ctrlAvg += coveredLength;
                    }
                } else {
                    coveredLength = 0;
                }

                addRegressionData(sr, lss, sample, coveredLength, exon.getLength());
            }

            caseAvg = MathManager.devide(caseAvg, SampleManager.getCaseNum());
            caseAvg = MathManager.devide(caseAvg, exon.getLength());
            ctrlAvg = MathManager.devide(ctrlAvg, SampleManager.getCtrlNum());
            ctrlAvg = MathManager.devide(ctrlAvg, exon.getLength());

            if (CoverageCommand.isMinCoverageFractionValid(caseAvg)
                    && CoverageCommand.isMinCoverageFractionValid(ctrlAvg)) {
                StringBuilder sb = new StringBuilder();
                String name = gene.getName() + "_" + exon.getId();
                sb.append(name).append(",");
                sb.append(exon.getChrStr()).append(",");
                sb.append(exon.getStartPosition()).append(",");
                sb.append(exon.getEndPosition()).append(",");
                sb.append(FormatManager.getFloat(caseAvg)).append(",");
                sb.append(FormatManager.getFloat(ctrlAvg)).append(",");

                float covDiff = Data.FLOAT_NA;

                if (CoverageCommand.isRelativeDifference) {
                    covDiff = MathManager.relativeDiff(caseAvg, ctrlAvg);
                } else {
                    covDiff = MathManager.abs(caseAvg, ctrlAvg);
                }

                sb.append(FormatManager.getFloat(covDiff)).append(",");
                sb.append(exon.getLength());

                addExon(sb, name, caseAvg, ctrlAvg, covDiff, exon.getLength(), sr, lss);

                bwCoverageSummaryByExon.write(sb.toString());
                bwCoverageSummaryByExon.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void addRegressionData(SimpleRegression sr, SummaryStatistics lss, Sample sample, int coveredLength, int exonLength) {
        if (CoverageCommand.isLinear) {
            float x = sample.getQuantitativeTrait();
            float y = MathManager.devide(coveredLength, exonLength);
            sr.addData(x, y);
            lss.addValue(y);
        }
    }

    private void addExon(StringBuilder sb, String name, float caseAvg, float ctrlAvg, float covDiff, int regionSize,
            SimpleRegression sr, SummaryStatistics lss) {
        if (CoverageCommand.isLinear) {
            double r2 = sr.getRSquare();
            double pValue = sr.getSignificance();
            double variance = lss.getVariance();
            if (Double.isNaN(pValue)) { //happens if all coverages are the same
                pValue = 1;
                r2 = 0;
            } else {
                r2 *= 100;
            }
            sb.append(",").append(pValue);
            sb.append(",").append(r2);
            sb.append(",").append(variance);

            exonCleanLinear.addExon(name, caseAvg, ctrlAvg, covDiff,
                    regionSize, pValue, r2, variance);
        } else {
            regionClean.addExon(name, caseAvg, ctrlAvg, covDiff, regionSize);
        }
    }

    private void outputCleanedExonListLinear() {
        try {
            exonCleanLinear.initCleanedRegionMap();
            exonCleanLinear.outputLog();

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                writeToFile(exonCleanLinear.getCleanedGeneStrByExon(gene), bwExonClean);

                writeToFile(exonCleanLinear.getCleanedGeneSummaryStrByExon(gene), bwGeneSummaryClean);
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void outputCleanedExonList() {
        try {
            regionClean.initCleanedRegionMap();
            regionClean.outputLog();

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                writeToFile(regionClean.getCleanedGeneStrByExon(gene), bwExonClean);

                writeToFile(regionClean.getCleanedGeneSummaryStrByExon(gene), bwGeneSummaryClean);
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public String toString() {
        return "Start running coverage comparison function";
    }
}
