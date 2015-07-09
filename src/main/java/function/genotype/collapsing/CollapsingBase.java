package function.genotype.collapsing;

import function.genotype.vargeno.SampleVariantCount;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.AnalysisBase4CalledVar;
import function.coverage.base.Gene;
import function.annotation.base.GeneManager;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;

/**
 *
 * @author nick
 */
public class CollapsingBase extends AnalysisBase4CalledVar {

    BufferedWriter bwGeneSampleMatrix = null;
    BufferedWriter bwSummary = null;
    BufferedWriter bwSampleVariantCount = null;
    final String geneSampleMatrixFilePath = CommonCommand.outputPath + "gene.sample.matrix.txt";
    final String summaryFilePath = CommonCommand.outputPath + "summary.csv";
    final String sampleVariantCountFilePath = CommonCommand.outputPath + "sample.variant.count.csv";
    final String fetPQQPlotPath = CommonCommand.outputPath + "summary.fet.p.qq.plot.pdf";
    final String linearPQQPlotPath = CommonCommand.outputPath + "summary.linear.p.qq.plot.pdf";
    final String logisticPQQPlotPath = CommonCommand.outputPath + "summary.logistic.p.qq.plot.pdf";
    ArrayList<CollapsingSummary> summaryList = new ArrayList<CollapsingSummary>();
    Hashtable<String, CollapsingSummary> summaryTable = new Hashtable<String, CollapsingSummary>();

    @Override
    public void initOutput() {
        try {
            bwGeneSampleMatrix = new BufferedWriter(new FileWriter(geneSampleMatrixFilePath));
            bwGeneSampleMatrix.write("sample/gene" + "\t");

            for (Sample sample : SampleManager.getList()) {
                bwGeneSampleMatrix.write(sample.getName() + "\t");
            }

            bwGeneSampleMatrix.newLine();

            bwSummary = new BufferedWriter(new FileWriter(summaryFilePath));
            bwSummary.write(CollapsingSummary.title);

            bwSampleVariantCount = new BufferedWriter(new FileWriter(sampleVariantCountFilePath));
            bwSampleVariantCount.write(SampleVariantCount.title);
            bwSampleVariantCount.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {
        try {
            bwSummary.flush();
            bwSummary.close();
            bwSampleVariantCount.flush();
            bwSampleVariantCount.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        generatePvaluesQQPlot();

        gzipFiles();
    }

    @Override
    public void beforeProcessDatabaseData() {
        initSummaryTable();

        SampleManager.generateCovariateFile();

        SampleVariantCount.init();
    }

    @Override
    public void afterProcessDatabaseData() {
        outputSummary();

        outputSampleVariantCount();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {

    }

    private void initSummaryTable() {
        for (HashSet<Gene> geneSet : GeneManager.getMap().values()) {
            for (Gene gene : geneSet) {
                updateSummaryTable(gene.getName());
            }
        }
    }

    public void updateSummaryTable(String geneName) {
        if (!summaryTable.containsKey(geneName)) {
            summaryTable.put(geneName, new CollapsingSummary(geneName));
        }
    }

    public void outputSummary() {
        try {
            LogManager.writeAndPrint("Output the data to matrix & summary file...");

            summaryList.addAll(summaryTable.values());

            outputMatrix();

            CollapsingSummary.calculateLinearAndLogisticP(geneSampleMatrixFilePath, summaryTable);

            Collections.sort(summaryList);

            int rank = 1;
            for (CollapsingSummary summary : summaryList) {
                bwSummary.write(rank++ + ",");
                bwSummary.write(summary.toString());
                bwSummary.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputSampleVariantCount() {
        try {
            for (Sample sample : SampleManager.getList()) {
                bwSampleVariantCount.write(sample.getName() + ",");
                bwSampleVariantCount.write(SampleVariantCount.getString(sample.getIndex()));
                bwSampleVariantCount.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputMatrix() throws Exception {
        for (CollapsingSummary summary : summaryList) {
            bwGeneSampleMatrix.write(summary.geneName + "\t");

            for (int s = 0; s < SampleManager.getListSize(); s++) {
                bwGeneSampleMatrix.write(summary.variantNumBySample[s] + "\t");
            }

            bwGeneSampleMatrix.newLine();

            summary.countSample();

            summary.calculateFetP();
        }

        bwGeneSampleMatrix.flush();
        bwGeneSampleMatrix.close();
    }

    private void generatePvaluesQQPlot() {
        ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingSummary.title,
                "Fet P", summaryFilePath, fetPQQPlotPath);

        if (CollapsingCommand.isCollapsingDoLogistic) {
            ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingSummary.title,
                    "Logistic P", summaryFilePath, logisticPQQPlotPath);
        } else if (CollapsingCommand.isCollapsingDoLinear) {
            ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingSummary.title,
                    "Linear P", summaryFilePath, linearPQQPlotPath);
        }
    }

    private void gzipFiles() {
        ThirdPartyToolManager.gzipFile(geneSampleMatrixFilePath);
    }
}
