package function.cohort.collapsing;

import function.annotation.base.Annotation;
import function.annotation.base.GeneManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import function.cohort.vargeno.ListVarGenoLite;
import function.external.exac.ExAC;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;
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
            bwSummary.write(CollapsingGeneSummary.getTitle());
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

            Reader in = new FileReader(GenotypeLevelFilterCommand.genotypeFile);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader(HEADERS)
                    .withFirstRecordAsHeader()
                    .parse(in);

            boolean isHeaderOutput = false;

            String processedVariantID = "";
            for (CSVRecord record : records) {
                if (!isHeaderOutput) {
                    outputHeader(record);
                    isHeaderOutput = true;
                }

                String variantID = record.get(VARIANT_ID_HEADER);
                String[] tmp = variantID.split("-");
                String chr = tmp[0];
                int pos = Integer.valueOf(tmp[1]);
                String ref = tmp[2];
                String alt = tmp[3];
                
                // loo af filter
                float looAF = FormatManager.getFloat(record.get(LOO_AF_HEADER));
                if (!CohortLevelFilterCommand.isMaxLooAFValid(looAF)) {
                    continue;
                }
                
                // ExAC filter
                ExAC exac = new ExAC(chr, pos, ref, alt, record);
                if(!exac.isValid()) {
                    continue;
                }

                StringJoiner allGeneTranscriptSJ = new StringJoiner(";");
                List<String> geneList = new ArrayList();
                Annotation mostDamagingAnnotation = new Annotation();
                String allAnnotation = record.get(ALL_ANNOTATION_HEADER);
                processAnnotation(
                        allAnnotation,
                        chr,
                        pos,
                        allGeneTranscriptSJ,
                        geneList,
                        mostDamagingAnnotation);

                if (geneList.isEmpty()) {
                    continue;
                }

                for (String geneName : geneList) {
                    String sampleName = record.get(SAMPLE_NAME_HEADER);
                    Sample sample = SampleManager.getSampleByName(sampleName);

                    if (!summaryMap.containsKey(geneName)) {
                        summaryMap.put(geneName, new CollapsingGeneSummary(geneName));
                    }

                    CollapsingSummary summary = summaryMap.get(geneName);
                    summary.updateSampleVariantCount4SingleVar(sample.getIndex());

                    // only count variant once per gene
                    if (!processedVariantID.equals(variantID)) {
                        boolean isSnv = ref.length() == alt.length();
                        summary.updateVariantCount(isSnv);
                    }
                }

                // output qualifed record to genotypes file
                outputGenotype(record, mostDamagingAnnotation, allGeneTranscriptSJ.toString());

                processedVariantID = variantID;
            }

            outputSummary();
            closeOutput();

            if (CollapsingCommand.isMannWhitneyTest) {
                ThirdPartyToolManager.runMannWhitneyTest(genotypeFilePath);
            }
            generatePvaluesQQPlot();
            gzipFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initGeneSummaryMap() {
        GeneManager.getMap().values().stream().forEach((geneSet) -> {
            geneSet.stream().forEach((gene) -> {
                if (!summaryMap.containsKey(gene.getName())) {
                    summaryMap.put(gene.getName(), new CollapsingGeneSummary(gene.getName()));
                }
            });
        });
    }

    public void outputSummary() {
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

    public void outputMatrix() throws Exception {
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

    public void generatePvaluesQQPlot() {
        ThirdPartyToolManager.generateQQPlot4CollapsingFetP(summaryFilePath, matrixFilePath, geneFetPQQPlotPath);
    }

    public void gzipFiles() {
        ThirdPartyToolManager.gzipFile(matrixFilePath);
    }
}
