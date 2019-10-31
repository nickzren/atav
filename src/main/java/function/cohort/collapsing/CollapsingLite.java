package function.cohort.collapsing;

import function.annotation.base.GeneManager;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import function.cohort.vargeno.ListVarGenoLite;
import function.cohort.vargeno.VariantLite;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.csv.CSVRecord;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class CollapsingLite extends ListVarGenoLite {

    private static BufferedWriter bwSampleMatrix = null;
    private static BufferedWriter bwSummary = null;

    private static final String matrixFilePath = CommonCommand.outputPath + "matrix.txt";
    private static final String summaryFilePath = CommonCommand.outputPath + "summary.csv";
    private static final String geneFetPQQPlotPath = CommonCommand.outputPath + "summary.fet.p.qq.plot.pdf";

    private static ArrayList<CollapsingSummary> summaryList = new ArrayList<>();
    private static HashMap<String, CollapsingSummary> summaryMap = new HashMap<>();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwSampleMatrix = new BufferedWriter(new FileWriter(matrixFilePath));
            bwSummary = new BufferedWriter(new FileWriter(summaryFilePath));

            bwSampleMatrix.write("sample/gene" + "\t");
            bwSummary.write(CollapsingGeneSummary.getHeader());
            bwSummary.newLine();

            for (Sample sample : SampleManager.getList()) {
                bwSampleMatrix.write(sample.getName() + "\t");
            }
            bwSampleMatrix.newLine();
        } catch (IOException ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            super.closeOutput();

            bwSummary.flush();
            bwSummary.close();
        } catch (IOException ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void run() {
        try {
            LogManager.writeAndPrint("Start running collapsing lite function");

            initOutput();

            initGeneSummaryMap();

            boolean isHeaderOutput = false;
            String previousVariantID = "";
            Iterable<CSVRecord> records = getRecords();
            for (CSVRecord record : records) {
                if (!isHeaderOutput) {
                    outputHeader(record);
                    isHeaderOutput = true;
                }

                VariantLite variantLite = new VariantLite(record);

                if (variantLite.isValid()) {
                    updateGeneSummary(variantLite, previousVariantID);
                    
                    // output qualifed record to genotypes file
                    outputGenotype(variantLite);
                }

                previousVariantID = variantLite.getVariantID();
            }

            outputSummary();
            closeOutput();

            if (CollapsingCommand.isMannWhitneyTest) {
                ThirdPartyToolManager.runMannWhitneyTest(genotypeLiteFilePath);
            }
            generatePvaluesQQPlot();
            gzipFiles();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initGeneSummaryMap() {
        GeneManager.getMap().values().stream().forEach((geneSet) -> {
            geneSet.stream().forEach((gene) -> {
                if (!summaryMap.containsKey(gene.getName())) {
                    summaryMap.put(gene.getName(), new CollapsingGeneSummary(gene.getName()));
                }
            });
        });
    }

    private void updateGeneSummary(VariantLite variantLite, String previousVariantID) {
        for (String geneName : variantLite.getGeneList()) {
            String sampleName = variantLite.getRecord().get(SAMPLE_NAME_HEADER);
            Sample sample = SampleManager.getSampleByName(sampleName);

            if (!summaryMap.containsKey(geneName)) {
                summaryMap.put(geneName, new CollapsingGeneSummary(geneName));
            }

            CollapsingSummary summary = summaryMap.get(geneName);
            summary.updateSampleVariantCount4SingleVar(sample.getIndex());

            // only count variant once per gene
            if (!previousVariantID.equals(variantLite.getVariantID())) {
                summary.updateVariantCount(variantLite.isSNV());
            }
        }
    }

    private void outputSummary() {
        LogManager.writeAndPrint("Output the data to matrix & summary file");

        try {
            summaryList.addAll(summaryMap.values());

            outputMatrix();

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

    private void outputMatrix() throws Exception {
        for (CollapsingSummary summary : summaryList) {
            bwSampleMatrix.write(summary.name + "\t");

            for (Sample sample : SampleManager.getList()) {
                bwSampleMatrix.write(summary.variantNumBySample[sample.getIndex()] + "\t");
            }

            bwSampleMatrix.newLine();

            summary.countSample();

            try {
                summary.calculateFetP();
            } catch (Exception e) {
                System.out.println();
            }
        }

        bwSampleMatrix.flush();
        bwSampleMatrix.close();
    }

    private void generatePvaluesQQPlot() {
        ThirdPartyToolManager.generateQQPlot4CollapsingFetP(summaryFilePath, matrixFilePath, geneFetPQQPlotPath);
    }

    private void gzipFiles() {
        ThirdPartyToolManager.gzipFile(matrixFilePath);
    }
}
