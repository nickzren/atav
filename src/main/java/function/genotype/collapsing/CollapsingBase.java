package function.genotype.collapsing;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.AnalysisBase4CalledVar;
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
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class CollapsingBase extends AnalysisBase4CalledVar {

    BufferedWriter bwSampleMatrix = null;
    BufferedWriter bwSummary = null;

    final String matrixFilePath = CommonCommand.outputPath + "matrix.txt";
    final String summaryFilePath = CommonCommand.outputPath + "summary.csv";
    final String geneFetPQQPlotPath = CommonCommand.outputPath + "summary.fet.p.qq.plot.pdf";
    final String geneLinearPQQPlotPath = CommonCommand.outputPath + "summary.linear.p.qq.plot.pdf";
    final String geneLogisticPQQPlotPath = CommonCommand.outputPath + "summary.logistic.p.qq.plot.pdf";

    ArrayList<CollapsingSummary> summaryList = new ArrayList<>();
    HashMap<String, CollapsingSummary> summaryMap = new HashMap<>();

    @Override
    public void initOutput() {
        try {
            bwSampleMatrix = new BufferedWriter(new FileWriter(matrixFilePath));
            bwSummary = new BufferedWriter(new FileWriter(summaryFilePath));

            if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
                bwSampleMatrix.write("sample/gene" + "\t");
                bwSummary.write(CollapsingGeneSummary.getTitle());
            } else {
                bwSampleMatrix.write("sample/region boundary" + "\t");
                bwSummary.write(CollapsingRegionSummary.getTitle());
            }
            bwSummary.newLine();

            for (Sample sample : SampleManager.getList()) {
                bwSampleMatrix.write(sample.getName() + "\t");
            }
            bwSampleMatrix.newLine();
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
        RegionBoundaryManager.init();

        initSummaryMap();

        SampleManager.generateCovariateFile();
    }

    @Override
    public void afterProcessDatabaseData() {
        outputSummary();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {

    }

    private void initSummaryMap() {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            initGeneSummaryMap();
        } else {
            initRegionSummaryMap();
        }
    }

    private void initGeneSummaryMap() {
        GeneManager.getMap().values().stream().forEach((geneSet) -> {
            geneSet.stream().forEach((gene) -> {
                updateGeneSummaryMap(gene.getName());
            });
        });
    }

    private void initRegionSummaryMap() {
        RegionBoundaryManager.getList().stream().forEach((regionBoundary) -> {
            updateRegionSummaryMap(regionBoundary.getName());
        });
    }

    public void updateGeneSummaryMap(String geneName) {
        if (!summaryMap.containsKey(geneName)) {
            summaryMap.put(geneName, new CollapsingGeneSummary(geneName));
        }
    }

    public void updateRegionSummaryMap(String regionName) {
        if (!summaryMap.containsKey(regionName)) {
            summaryMap.put(regionName, new CollapsingRegionSummary(regionName));
        }
    }

    public void outputSummary() {
        LogManager.writeAndPrint("Output the data to matrix & summary file");

        try {
            summaryList.addAll(summaryMap.values());

            outputMatrix();

            if (CollapsingCommand.regionBoundaryFile.isEmpty()) { // gene summary
                CollapsingGeneSummary.calculateLinearAndLogisticP(matrixFilePath, summaryMap);
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

            for (Sample sample : SampleManager.getList()) {
                bwSampleMatrix.write(summary.variantNumBySample[sample.getIndex()] + "\t");
            }

            bwSampleMatrix.newLine();

            summary.countSample();

            summary.calculateFetP();
        }

        bwSampleMatrix.flush();
        bwSampleMatrix.close();
    }

    private void generatePvaluesQQPlot() {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            ThirdPartyToolManager.generateQQPlot4CollapsingFetP(summaryFilePath, matrixFilePath, geneFetPQQPlotPath);

            if (CollapsingCommand.isCollapsingDoLogistic) {
                ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.getTitle(),
                        "Logistic P", summaryFilePath, geneLogisticPQQPlotPath);
            } else if (CollapsingCommand.isCollapsingDoLinear) {
                ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.getTitle(),
                        "Linear P", summaryFilePath, geneLinearPQQPlotPath);
            }
        }
    }

    private void gzipFiles() {
        ThirdPartyToolManager.gzipFile(matrixFilePath);
    }
}
