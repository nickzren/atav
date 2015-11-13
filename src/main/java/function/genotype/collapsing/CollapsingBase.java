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
    BufferedWriter bwGeneSummary = null;
    BufferedWriter bwRegionSampleMatrix = null;
    BufferedWriter bwRegionSummary = null;
    BufferedWriter bwSampleVariantCount = null;
    final String geneSampleMatrixFilePath = CommonCommand.outputPath + "gene.sample.matrix.txt";
    final String geneSummaryFilePath = CommonCommand.outputPath + "gene.summary.csv";
    final String regionSampleMatrixFilePath = CommonCommand.outputPath + "region.sample.matrix.txt";
    final String regionSummaryFilePath = CommonCommand.outputPath + "region.summary.csv";
    final String sampleVariantCountFilePath = CommonCommand.outputPath + "sample.variant.count.csv";
    final String geneFetPQQPlotPath = CommonCommand.outputPath + "gene.summary.fet.p.qq.plot.pdf";
    final String geneLinearPQQPlotPath = CommonCommand.outputPath + "gene.summary.linear.p.qq.plot.pdf";
    final String geneLogisticPQQPlotPath = CommonCommand.outputPath + "gene.summary.logistic.p.qq.plot.pdf";
    ArrayList<CollapsingGeneSummary> geneSummaryList = new ArrayList<CollapsingGeneSummary>();
    Hashtable<String, CollapsingGeneSummary> geneSummaryTable = new Hashtable<String, CollapsingGeneSummary>();
    ArrayList<CollapsingRegionSummary> regionSummaryList = new ArrayList<CollapsingRegionSummary>();
    Hashtable<String, CollapsingRegionSummary> regionSummaryTable = new Hashtable<String, CollapsingRegionSummary>();

    @Override
    public void initOutput() {
        try {
            if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
                bwGeneSampleMatrix = new BufferedWriter(new FileWriter(geneSampleMatrixFilePath));
                bwGeneSampleMatrix.write("sample/gene" + "\t");
                for (Sample sample : SampleManager.getList()) {
                    bwGeneSampleMatrix.write(sample.getName() + "\t");
                }
                bwGeneSampleMatrix.newLine();

                bwGeneSummary = new BufferedWriter(new FileWriter(geneSummaryFilePath));
                bwGeneSummary.write(CollapsingGeneSummary.title);
            } else {
                bwRegionSampleMatrix = new BufferedWriter(new FileWriter(regionSampleMatrixFilePath));
                bwRegionSampleMatrix.write("sample/region boundary" + "\t");
                for (Sample sample : SampleManager.getList()) {
                    bwRegionSampleMatrix.write(sample.getName() + "\t");
                }
                bwRegionSampleMatrix.newLine();

                bwRegionSummary = new BufferedWriter(new FileWriter(regionSummaryFilePath));
                bwRegionSummary.write(CollapsingRegionSummary.title);
            }

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
            if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
                bwGeneSummary.flush();
                bwGeneSummary.close();
            } else {
                bwRegionSummary.flush();
                bwRegionSummary.close();
            }
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
        if (!geneSummaryTable.containsKey(geneName)) {
            geneSummaryTable.put(geneName, new CollapsingGeneSummary(geneName));
        }
    }

    public void updateRegionSummaryTable(String regionName) {
        if (!regionSummaryTable.containsKey(regionName)) {
            regionSummaryTable.put(regionName, new CollapsingRegionSummary(regionName));
        }
    }

    public void outputSummary() {
        LogManager.writeAndPrint("Output the data to matrix & summary file...");

        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            outputGeneSummary();
        } else {
            outputRegionSummary();
        }
    }

    public void outputGeneSummary() {
        try {
            geneSummaryList.addAll(geneSummaryTable.values());

            outputGeneMatrix();

            CollapsingGeneSummary.calculateLinearAndLogisticP(geneSampleMatrixFilePath, geneSummaryTable);

            Collections.sort(geneSummaryList);

            int rank = 1;
            for (CollapsingGeneSummary summary : geneSummaryList) {
                bwGeneSummary.write(rank++ + ",");
                bwGeneSummary.write(summary.toString());
                bwGeneSummary.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputRegionSummary() {
        try {
            regionSummaryList.addAll(regionSummaryTable.values());

            outputRegionMatrix();

            Collections.sort(regionSummaryList);

            int rank = 1;
            for (CollapsingSummary summary : regionSummaryList) {
                bwRegionSummary.write(rank++ + ",");
                bwRegionSummary.write(summary.toString());
                bwRegionSummary.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void outputGeneMatrix() throws Exception {
        for (CollapsingGeneSummary summary : geneSummaryList) {
            bwGeneSampleMatrix.write(summary.name + "\t");

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
    
    public void outputRegionMatrix() throws Exception {
        for (CollapsingRegionSummary summary : regionSummaryList) {
            bwRegionSampleMatrix.write(summary.name + "\t");

            for (int s = 0; s < SampleManager.getListSize(); s++) {
                bwRegionSampleMatrix.write(summary.variantNumBySample[s] + "\t");
            }

            bwRegionSampleMatrix.newLine();

            summary.countSample();

            summary.calculateFetP();
        }

        bwRegionSampleMatrix.flush();
        bwRegionSampleMatrix.close();
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
                    "Fet P", geneSummaryFilePath, geneFetPQQPlotPath);

            if (CollapsingCommand.isCollapsingDoLogistic) {
                ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.title,
                        "Logistic P", geneSummaryFilePath, geneLogisticPQQPlotPath);
            } else if (CollapsingCommand.isCollapsingDoLinear) {
                ThirdPartyToolManager.generatePvaluesQQPlot(CollapsingGeneSummary.title,
                        "Linear P", geneSummaryFilePath, geneLinearPQQPlotPath);
            }
        }
    }

    private void gzipFiles() {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            ThirdPartyToolManager.gzipFile(geneSampleMatrixFilePath);
        }else{
            ThirdPartyToolManager.gzipFile(regionSampleMatrixFilePath);
        }
    }
}
