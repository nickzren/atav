package function.cohort.collapsing;

import function.annotation.base.AnnotationLevelFilterCommand;
import function.annotation.base.GeneManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import function.cohort.vcf.ListVCFLite;
import function.cohort.vcf.VariantVCFLite;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class CollapsingVCFLite extends ListVCFLite {

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
            LogManager.writeAndPrint("Start running collapsing vcf lite function");

            init();

            File f = new File(GenotypeLevelFilterCommand.vcfFile);
            Reader decoder;
            if (f.getName().endsWith(".gz")) {
                InputStream fileStream = new FileInputStream(f);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                decoder = new InputStreamReader(gzipStream);
            } else {
                decoder = new FileReader(f);
            }
            BufferedReader br = new BufferedReader(decoder);

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                String[] values = lineStr.split("\t");
                VariantVCFLite variantLite = new VariantVCFLite(values);

                // output qualifed record to vcf file
                if (variantLite.isValid()) {
                    if (!AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
                        updateTranscriptSummary(variantLite);
                    } else {
                        updateGeneSummary(variantLite);
                    }

                    // output qualifed record to vcf file
                    bwVCF.write(variantLite.toString());
                    bwVCF.newLine();
                }
            }

            br.close();
            decoder.close();

            outputSummary();
            closeOutput();

            generatePvaluesQQPlot();
            gzipFiles();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void updateGeneSummary(VariantVCFLite variantLite) {
        for (String geneName : variantLite.getGeneList()) {
            summaryMap.putIfAbsent(geneName, new CollapsingGeneSummary(geneName));

            countQV(geneName, variantLite);
        }
    }

    private void updateTranscriptSummary(VariantVCFLite variantLite) {
        for (int id : variantLite.getTranscriptSet()) {
            String idStr = String.valueOf(id);
            summaryMap.putIfAbsent(idStr, new CollapsingTranscriptSummary(idStr));

            countQV(idStr, variantLite);
        }
    }

    private void countQV(String collapsingKey, VariantVCFLite variantLite) {
        CollapsingSummary summary = summaryMap.get(collapsingKey);
        summary.updateVariantCount(variantLite.isSNV());

        for (int i = 0; i < variantLite.getGTArr().length; i++) {
            byte geno = variantLite.getGTArr()[i];
            if (GenotypeLevelFilterCommand.isQualifiedGeno(geno)) {
                summary.countQualifiedVariantBySample(geno, i);
            }
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
