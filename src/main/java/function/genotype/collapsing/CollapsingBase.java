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

    BufferedWriter bwSampleMatrix = null;
    BufferedWriter bwSummary = null;
    BufferedWriter bwSampleVariantCount = null;

    final String sampleMatrixFilePath = CommonCommand.outputPath + "sample.matrix.txt";
    final String summaryFilePath = CommonCommand.outputPath + "summary.csv";
    final String sampleVariantCountFilePath = CommonCommand.outputPath + "sample.variant.count.csv";
    final String geneFetPQQPlotPath = CommonCommand.outputPath + "summary.fet.p.qq.plot.pdf";
    final String geneLinearPQQPlotPath = CommonCommand.outputPath + "summary.linear.p.qq.plot.pdf";
    final String geneLogisticPQQPlotPath = CommonCommand.outputPath + "summary.logistic.p.qq.plot.pdf";

    ArrayList<CollapsingSummary> summaryList = new ArrayList<CollapsingSummary>();
    Hashtable<String, CollapsingSummary> summaryTable = new Hashtable<String, CollapsingSummary>();

    @Override
    public void initOutput() {
        try {
            bwSampleMatrix = new BufferedWriter(new FileWriter(sampleMatrixFilePath));
            bwSummary = new BufferedWriter(new FileWriter(summaryFilePath));

            if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
                bwSampleMatrix.write("sample/gene" + "\t");
                bwSummary.write(CollapsingGeneSummary.title);
            } else {
                bwSampleMatrix.write("sample/region boundary" + "\t");
                bwSummary.write(CollapsingRegionSummary.title);
            }

            for (Sample sample : SampleManager.getList()) {
                bwSampleMatrix.write(sample.getName() + "\t");
            }
            bwSampleMatrix.newLine();

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
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            initGeneSummaryTable();
        } else {
            initRegionSummaryTable();
        }
    }

    private void initGeneSummaryTable() {
        for (HashSet<Gene> geneSet : GeneManager.getMap().values()) {
            for (Gene gene : geneSet) {
                updateGeneSummaryTable(gene.getName());
            }
        }
    }

    private void initRegionSummaryTable() {
        for (RegionBoundary regionBoundary : RegionBoundaryManager.getList()) {
            updateRegionSummaryTable(regionBoundary.getName());
        }
    }

    public void updateGeneSummaryTable(String geneName) {
        if (!summaryTable.containsKey(geneName)) {
            summaryTable.put(geneName, new CollapsingGeneSummary(geneName));
        }
    }

    public void updateRegionSummaryTable(String regionName) {
        if (!summaryTable.containsKey(regionName)) {
            summaryTable.put(regionName, new CollapsingRegionSummary(regionName));
        }
    }

    public void outputSummary() {
        LogManager.writeAndPrint("Output the data to matrix & summary file...");

        try {
            summaryList.addAll(summaryTable.values());

            outputMatrix();

            if (CollapsingCommand.regionBoundaryFile.isEmpty()) { // gene summary
                CollapsingGeneSummary.calculateLinearAndLogisticP(sampleMatrixFilePath, summaryTable);
            }

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

    public void outputMatrix() throws Exception {
        for (CollapsingSummary summary : summaryList) {
            bwSampleMatrix.write(summary.name + "\t");

            for (int s = 0; s < SampleManager.getListSize(); s++) {
                bwSampleMatrix.write(summary.variantNumBySample[s] + "\t");
            }

            bwSampleMatrix.newLine();

            summary.countSample();

            summary.calculateFetP();
        }

        bwSampleMatrix.flush();
        bwSampleMatrix.close();
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

    private void generatePvaluesQQPlot() {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.title,
                    "Fet P", summaryFilePath, geneFetPQQPlotPath);

            if (CollapsingCommand.isCollapsingDoLogistic) {
                ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.title,
                        "Logistic P", summaryFilePath, geneLogisticPQQPlotPath);
            } else if (CollapsingCommand.isCollapsingDoLinear) {
                ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.title,
                        "Linear P", summaryFilePath, geneLinearPQQPlotPath);
            }
        }
    }

    private void gzipFiles() {
        ThirdPartyToolManager.gzipFile(sampleMatrixFilePath);
    }
}
