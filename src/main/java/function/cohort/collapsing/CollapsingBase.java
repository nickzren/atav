package function.cohort.collapsing;

import function.annotation.base.AnnotationLevelFilterCommand;
import function.cohort.base.CalledVariant;
import function.cohort.base.Sample;
import function.cohort.base.AnalysisBase4CalledVar;
import function.annotation.base.GeneManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.SampleManager;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarManager;
import function.variant.base.RegionManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

            if (!CollapsingCommand.regionBoundaryFile.isEmpty()) {
                bwSampleMatrix.write("sample/region" + "\t");
                bwSummary.write(CollapsingRegionSummary.getHeader());
            } else if (!AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
                bwSampleMatrix.write("sample/transcript" + "\t");
                bwSummary.write(CollapsingTranscriptSummary.getHeader());
            } else { // default is gene collapsing 
                bwSampleMatrix.write("sample/gene" + "\t");
                bwSummary.write(CollapsingGeneSummary.getHeader());
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
        gzipFiles();

        generatePvaluesQQPlot();
    }

    @Override
    public void beforeProcessDatabaseData() {
        RegionBoundaryManager.init();

        initSummaryMap();

        SampleManager.generateCovariateFile();

        if (KnownVarCommand.isIncludeOMIM) {
            KnownVarManager.initOMIMMap();
        }
    }

    @Override
    public void afterProcessDatabaseData() {
        outputSummary();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {

    }

    private void initSummaryMap() {
        if (!CollapsingCommand.regionBoundaryFile.isEmpty()) {
            initRegionSummaryMap();
        } else if (!AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
            initTranscriptSummaryMap();
        } else {
            initGeneSummaryMap();
        }
    }

    private void initGeneSummaryMap() {
        GeneManager.getMap().values().stream().forEach((geneSet) -> {
            geneSet.stream().forEach((gene) -> {
                summaryMap.putIfAbsent(gene.getName(), new CollapsingGeneSummary(gene.getName()));
            });
        });
    }

    private void initRegionSummaryMap() {
        for (String chr : RegionManager.getChrList()) {
            List<RegionBoundary> list = RegionBoundaryManager.getList(chr);
            if (list != null) {
                list.stream().forEach((regionBoundary) -> {
                    summaryMap.putIfAbsent(regionBoundary.getName(), new CollapsingGeneSummary(regionBoundary.getName()));
                });
            }
        }
    }

    private void initTranscriptSummaryMap() {
        TranscriptManager.getTranscriptBoundaryMap().values().stream().forEach((transcriptBoundary) -> {
            String idStr = String.valueOf(transcriptBoundary.getId());
            summaryMap.putIfAbsent(idStr, new CollapsingTranscriptSummary(idStr));
        });
    }

    public void outputSummary() {
        LogManager.writeAndPrint("Output the data to matrix & summary file");

        try {
            summaryList.addAll(summaryMap.values());

            outputMatrix();

            if (CollapsingCommand.regionBoundaryFile.isEmpty()
                    && AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) { // gene summary
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
                bwSampleMatrix.write(summary.hasQualifiedVariantBySample[sample.getIndex()] + "\t");
            }

            bwSampleMatrix.newLine();

            summary.countSample();

            summary.calculateFetP();
        }

        bwSampleMatrix.flush();
        bwSampleMatrix.close();
    }

    private void generatePvaluesQQPlot() {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()
                && AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
            if (CollapsingCommand.isCollapsingDoLogistic) {
                ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.getHeader(),
                        "Logistic P", summaryFilePath, geneLogisticPQQPlotPath);
            } else if (CollapsingCommand.isCollapsingDoLinear) {
                ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.getHeader(),
                        "Linear P", summaryFilePath, geneLinearPQQPlotPath);
                // linear regression does not have cases and controls, so it skip to run fet p qq-plot script
                return;
            }

            ThirdPartyToolManager.generateQQPlot4CollapsingFetP(summaryFilePath, matrixFilePath, geneFetPQQPlotPath);
        }
    }

    private void gzipFiles() {
        ThirdPartyToolManager.gzipFile(matrixFilePath);
    }
}
