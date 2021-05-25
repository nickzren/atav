package function.cohort.collapsing;

import function.annotation.base.AnnotationLevelFilterCommand;
import function.annotation.base.GeneManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import function.cohort.vargeno.ListVarGenoLite;
import function.cohort.vargeno.VariantGenoLite;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarManager;
import global.Data;
import global.Index;
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

    private void init() {
        initOutput();

        initSummaryMap();

        if (KnownVarCommand.isIncludeOMIM) {
            KnownVarManager.initOMIMMap();
        }
    }

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwSampleMatrix = new BufferedWriter(new FileWriter(matrixFilePath));
            bwSummary = new BufferedWriter(new FileWriter(summaryFilePath));

            if (!AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
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

            init();

            boolean isHeaderOutput = false;
            String previousVariantID = Data.STRING_NA;
            Iterable<CSVRecord> records = getRecords();
            for (CSVRecord record : records) {
                if (!isHeaderOutput) {
                    outputHeader(record);
                    isHeaderOutput = true;
                }

                // each variantLite object represent one row data record in genotype file
                VariantGenoLite variantLite = new VariantGenoLite(record);

                if (variantLite.isValid()) {
                    if (!AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
                        updateTranscriptSummary(variantLite, previousVariantID);
                    } else {
                        updateGeneSummary(variantLite, previousVariantID);
                    }

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

    private void updateGeneSummary(VariantGenoLite variantLite, String previousVariantID) {
        for (String geneName : variantLite.getGeneList()) {
            summaryMap.putIfAbsent(geneName, new CollapsingGeneSummary(geneName));

            countQV(geneName, variantLite, previousVariantID);
        }
    }

    private void updateTranscriptSummary(VariantGenoLite variantLite, String previousVariantID) {
        for (int id : variantLite.getTranscriptSet()) {
            String idStr = String.valueOf(id);
            summaryMap.putIfAbsent(idStr, new CollapsingTranscriptSummary(idStr));

            countQV(idStr, variantLite, previousVariantID);
        }
    }

    private void countQV(String collapsingKey, VariantGenoLite variantLite, String previousVariantID) {
        String sampleName = variantLite.getRecord().get(SAMPLE_NAME_HEADER);
        Sample sample = SampleManager.getSampleByName(sampleName);

        String gtStr = variantLite.getRecord().get(GT_HEADER);
        byte geno = getGeno(gtStr);

        CollapsingSummary summary = summaryMap.get(collapsingKey);
        summary.countQualifiedVariantBySample(geno, sample.getIndex());

        // only count variant once per gene
        if (!previousVariantID.equals(variantLite.getVariantID())) {
            summary.updateVariantCount(variantLite.isSNV());
        }
    }

    private void initSummaryMap() {
        if (!AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
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

    private void initTranscriptSummaryMap() {
        TranscriptManager.getTranscriptBoundaryMap().values().stream().forEach((transcriptBoundary) -> {
            String idStr = String.valueOf(transcriptBoundary.getId());
            summaryMap.putIfAbsent(idStr, new CollapsingTranscriptSummary(idStr));
        });
    }

    private byte getGeno(String geno) {
        switch (geno) {
            case "hom":
                return Index.HOM;
            case "het":
                return Index.HET;
            case "hom ref":
                return Index.REF;
            default:
                return Data.BYTE_NA;
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
                bwSampleMatrix.write(summary.hasQualifiedVariantBySample[sample.getIndex()] + "\t");
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
        if (AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
            ThirdPartyToolManager.generateQQPlot4CollapsingFetP(summaryFilePath, matrixFilePath, geneFetPQQPlotPath);
        }
    }

    private void gzipFiles() {
        ThirdPartyToolManager.gzipFile(matrixFilePath);
    }
}
