package function.coverage.comparison;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.StringJoiner;
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

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwCoverageSummaryByExon = new BufferedWriter(new FileWriter(coverageSummaryByExon));
            bwCoverageSummaryByExon.write("EXON,Chr,Start,End,AvgCase,AvgCtrl,CovDiff,Length");
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
            outputCleanedExonList();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void processExon(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        try {
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
            }

            caseAvg = MathManager.devide(caseAvg, SampleManager.getCaseNum());
            caseAvg = MathManager.devide(caseAvg, exon.getLength());
            ctrlAvg = MathManager.devide(ctrlAvg, SampleManager.getCtrlNum());
            ctrlAvg = MathManager.devide(ctrlAvg, exon.getLength());

            StringJoiner sj = new StringJoiner(",");
            String name = gene.getName() + "_" + exon.getId();
            sj.add(name);
            sj.add(exon.getChrStr());
            sj.add(FormatManager.getInteger(exon.getStartPosition()));
            sj.add(FormatManager.getInteger(exon.getEndPosition()));
            sj.add(FormatManager.getFloat(caseAvg));
            sj.add(FormatManager.getFloat(ctrlAvg));

            float covDiff = Data.FLOAT_NA;

            if (CoverageCommand.isRelativeDifference) {
                covDiff = MathManager.relativeDiff(caseAvg, ctrlAvg);
            } else {
                covDiff = MathManager.abs(caseAvg, ctrlAvg);
            }

            sj.add(FormatManager.getFloat(covDiff));
            sj.add(FormatManager.getInteger(exon.getLength()));

            addExon(name, caseAvg, ctrlAvg, covDiff, exon.getLength());

            bwCoverageSummaryByExon.write(sj.toString());
            bwCoverageSummaryByExon.newLine();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void addExon(String name, float caseAvg, float ctrlAvg, float covDiff, int regionSize) {
        regionClean.addExon(name, caseAvg, ctrlAvg, covDiff, regionSize);
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
