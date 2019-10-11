package function.cohort.collapsing;

import function.annotation.base.Annotation;
import function.annotation.base.AnnotationLevelFilterCommand;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.annotation.base.PolyphenManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
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
import utils.MathManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class CollapsingLite {

    private static BufferedWriter bwGenotypes = null;
    private static BufferedWriter bwSampleMatrix = null;
    private static BufferedWriter bwSummary = null;

    private static final String genotypeFilePath = CommonCommand.outputPath + "genotypes.csv";
    private static final String matrixFilePath = CommonCommand.outputPath + "matrix.txt";
    private static final String summaryFilePath = CommonCommand.outputPath + "summary.csv";
    private static final String geneFetPQQPlotPath = CommonCommand.outputPath + "summary.fet.p.qq.plot.pdf";

    private static final String VARIANT_ID_HEADER = "Variant ID";
    private static final String STABLE_ID_HEADER = "Transcript Stable Id";
    private static int STABLE_ID_HEADER_INDEX;
    private static final String EFFECT_HEADER = "Effect";
    private static int EFFECT_HEADER_INDEX;
    private static final String HAS_CCDS_HEADER = "Has CCDS Transcript";
    private static int HAS_CCDS_HEADER_INDEX;
    private static final String HGVS_c_HEADER = "HGVS_c";
    private static int HGVS_c_HEADER_INDEX;
    private static final String HGVS_p_HEADER = "HGVS_p";
    private static int HGVS_p_HEADER_INDEX;
    private static final String POLYPHEN_HUMDIV_SCORE_HEADER = "Polyphen Humdiv Score";
    private static int POLYPHEN_HUMDIV_SCORE_HEADER_INDEX;
    private static final String POLYPHEN_HUMDIV_PREDICTION_HEADER = "Polyphen Humdiv Prediction";
    private static int POLYPHEN_HUMDIV_PREDICTION_HEADER_INDEX;
    private static final String POLYPHEN_HUMDIV_SCORE_CCDS_HEADER = "Polyphen Humdiv Score (CCDS)";
    private static int POLYPHEN_HUMDIV_SCORE_CCDS_HEADER_INDEX;
    private static final String POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER = "Polyphen Humdiv Prediction (CCDS)";
    private static int POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER_INDEX;
    private static final String POLYPHEN_HUMVAR_SCORE_HEADER = "Polyphen Humvar Score";
    private static int POLYPHEN_HUMVAR_SCORE_HEADER_INDEX;
    private static final String POLYPHEN_HUMVAR_PREDICTION_HEADER = "Polyphen Humvar Prediction";
    private static int POLYPHEN_HUMVAR_PREDICTION_HEADER_INDEX;
    private static final String POLYPHEN_HUMVAR_SCORE_CCDS_HEADER = "Polyphen Humvar Score (CCDS)";
    private static int POLYPHEN_HUMVAR_SCORE_CCDS_HEADER_INDEX;
    private static final String POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER = "Polyphen Humvar Prediction (CCDS)";
    private static int POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER_INDEX;
    private static final String GENE_NAME_HEADER = "Gene Name";
    private static int GENE_NAME_HEADER_INDEX;
    private static final String ALL_ANNOTATION_HEADER = "All Effect Gene Transcript HGVS_c HGVS_p Polyphen_Humdiv Polyphen_Humvar";
    private static int ALL_ANNOTATION_HEADER_INDEX;
    private static final String SAMPLE_NAME_HEADER = "Sample Name";
    private static final String LOO_AF_HEADER = "LOO AF";

    private static final String[] HEADERS = {
        VARIANT_ID_HEADER,
        STABLE_ID_HEADER,
        EFFECT_HEADER,
        HAS_CCDS_HEADER,
        HGVS_c_HEADER,
        HGVS_p_HEADER,
        POLYPHEN_HUMDIV_SCORE_HEADER,
        POLYPHEN_HUMDIV_PREDICTION_HEADER,
        POLYPHEN_HUMDIV_SCORE_CCDS_HEADER,
        POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER,
        POLYPHEN_HUMVAR_SCORE_HEADER,
        POLYPHEN_HUMVAR_PREDICTION_HEADER,
        POLYPHEN_HUMVAR_SCORE_CCDS_HEADER,
        POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER,
        GENE_NAME_HEADER,
        ALL_ANNOTATION_HEADER,
        SAMPLE_NAME_HEADER,
        LOO_AF_HEADER
    };

    private static ArrayList<CollapsingSummary> summaryList = new ArrayList<>();
    private static HashMap<String, CollapsingSummary> summaryMap = new HashMap<>();

    public static void initOutput() {
        try {
            bwGenotypes = new BufferedWriter(new FileWriter(genotypeFilePath));

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

    public static void closeOutput() {
        try {
            bwGenotypes.flush();
            bwGenotypes.close();

            bwSummary.flush();
            bwSummary.close();
        } catch (IOException ex) {
            ErrorManager.send(ex);
        }
    }

    public static void run() {
        try {
            LogManager.writeAndPrint("Start running collapsing lite function");

            initOutput();

            initGeneSummaryMap();

            Reader in = new FileReader(CollapsingCommand.genotypeFile);
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
                boolean isSnv = ref.length() == alt.length();

                // loo af filter
                float looAF = FormatManager.getFloat(record.get(LOO_AF_HEADER));
                if (!CohortLevelFilterCommand.isMaxLooAFValid(looAF)) {
                    continue;
                }

                StringJoiner allGeneTranscriptSJ = new StringJoiner(";");
                List<String> geneList = new ArrayList();

                Annotation mostDamagingAnnotation = new Annotation();
                boolean hasCCDS = false;

                String allAnnotation = record.get(ALL_ANNOTATION_HEADER);
                for (String annotation : allAnnotation.split(";")) {
                    String[] values = annotation.split("\\|");
                    String effect = values[0];
                    String geneName = values[1];
                    String stableId = values[2];
                    String HGVS_c = values[3];
                    String HGVS_p = values[4];
                    float polyphenHumdiv = FormatManager.getFloat(values[5]);
                    float polyphenHumvar = FormatManager.getFloat(values[6]);

                    // --effect filter applied
                    // --polyphen-humdiv filter applied
                    // --gene or --gene-boundary filter applied
                    if (EffectManager.isEffectContained(effect)
                            && PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                            && GeneManager.isValid(geneName, chr, pos)) {
                        if (mostDamagingAnnotation.effect == null) {
                            mostDamagingAnnotation.effect = effect;
                            mostDamagingAnnotation.stableId = FormatManager.getInteger(stableId);
                            mostDamagingAnnotation.HGVS_c = HGVS_c;
                            mostDamagingAnnotation.HGVS_p = HGVS_p;
                            mostDamagingAnnotation.geneName = geneName;
                        }

                        StringJoiner geneTranscriptSJ = new StringJoiner("|");
                        geneTranscriptSJ.add(effect);
                        geneTranscriptSJ.add(geneName);
                        geneTranscriptSJ.add(stableId);
                        geneTranscriptSJ.add(HGVS_c);
                        geneTranscriptSJ.add(HGVS_p);
                        geneTranscriptSJ.add(FormatManager.getFloat(polyphenHumdiv));
                        geneTranscriptSJ.add(FormatManager.getFloat(polyphenHumvar));

                        allGeneTranscriptSJ.add(geneTranscriptSJ.toString());
                        if (!geneList.contains(geneName)) {
                            geneList.add(geneName);
                        }

                        mostDamagingAnnotation.polyphenHumdiv = MathManager.max(mostDamagingAnnotation.polyphenHumdiv, polyphenHumdiv);
                        mostDamagingAnnotation.polyphenHumvar = MathManager.max(mostDamagingAnnotation.polyphenHumvar, polyphenHumvar);

                        boolean isCCDS = TranscriptManager.isCCDSTranscript(chr, FormatManager.getInteger(stableId));

                        if (isCCDS) {
                            mostDamagingAnnotation.polyphenHumdivCCDS = MathManager.max(mostDamagingAnnotation.polyphenHumdivCCDS, polyphenHumdiv);
                            mostDamagingAnnotation.polyphenHumvarCCDS = MathManager.max(mostDamagingAnnotation.polyphenHumvarCCDS, polyphenHumvar);

                            hasCCDS = true;
                        }
                    }
                }

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
                        summary.updateVariantCount(isSnv);
                    }
                }

                // output qualifed record to genotypes file
                outputGenotype(record, mostDamagingAnnotation, allGeneTranscriptSJ.toString(), hasCCDS);

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

    private static void outputHeader(CSVRecord record) throws IOException {
        StringJoiner sj = new StringJoiner(",");

        for (int headerIndex = 0; headerIndex < record.getParser().getHeaderNames().size(); headerIndex++) {
            String value = record.getParser().getHeaderNames().get(headerIndex);
            switch (value) {
                case STABLE_ID_HEADER:
                    STABLE_ID_HEADER_INDEX = headerIndex;
                    break;
                case EFFECT_HEADER:
                    EFFECT_HEADER_INDEX = headerIndex;
                    break;
                case HAS_CCDS_HEADER:
                    HAS_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case HGVS_c_HEADER:
                    HGVS_c_HEADER_INDEX = headerIndex;
                    break;
                case HGVS_p_HEADER:
                    HGVS_p_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMDIV_SCORE_HEADER:
                    POLYPHEN_HUMDIV_SCORE_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMDIV_PREDICTION_HEADER:
                    POLYPHEN_HUMDIV_PREDICTION_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMDIV_SCORE_CCDS_HEADER:
                    POLYPHEN_HUMDIV_SCORE_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER:
                    POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMVAR_SCORE_HEADER:
                    POLYPHEN_HUMVAR_SCORE_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMVAR_PREDICTION_HEADER:
                    POLYPHEN_HUMVAR_PREDICTION_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMVAR_SCORE_CCDS_HEADER:
                    POLYPHEN_HUMVAR_SCORE_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER:
                    POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case GENE_NAME_HEADER:
                    GENE_NAME_HEADER_INDEX = headerIndex;
                    break;
                case ALL_ANNOTATION_HEADER:
                    ALL_ANNOTATION_HEADER_INDEX = headerIndex;
                    break;
                default:
                    break;
            }

            sj.add(value);
        }

        bwGenotypes.write(sj.toString());
        bwGenotypes.newLine();
    }

    private static void outputGenotype(CSVRecord record, Annotation mostDamagingAnnotation, String allAnnotation, boolean hasCCDS) throws IOException {
        StringJoiner sj = new StringJoiner(",");

        for (int headerIndex = 0; headerIndex < record.size(); headerIndex++) {
            String value = "";

            if (headerIndex == STABLE_ID_HEADER_INDEX) {
                value = mostDamagingAnnotation.getStableId();
            } else if (headerIndex == EFFECT_HEADER_INDEX) {
                value = mostDamagingAnnotation.effect;
            } else if (headerIndex == HAS_CCDS_HEADER_INDEX) {
                value = Boolean.toString(hasCCDS);
            } else if (headerIndex == HGVS_c_HEADER_INDEX) {
                value = mostDamagingAnnotation.HGVS_c;
            } else if (headerIndex == HGVS_p_HEADER_INDEX) {
                value = mostDamagingAnnotation.HGVS_p;
            } else if (headerIndex == POLYPHEN_HUMDIV_SCORE_HEADER_INDEX) {
                value = FormatManager.getFloat(mostDamagingAnnotation.polyphenHumdiv);
            } else if (headerIndex == POLYPHEN_HUMDIV_PREDICTION_HEADER_INDEX) {
                value = PolyphenManager.getPrediction(mostDamagingAnnotation.polyphenHumdiv, mostDamagingAnnotation.effect);
            } else if (headerIndex == POLYPHEN_HUMDIV_SCORE_CCDS_HEADER_INDEX) {
                value = FormatManager.getFloat(mostDamagingAnnotation.polyphenHumdivCCDS);
            } else if (headerIndex == POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER_INDEX) {
                value = PolyphenManager.getPrediction(mostDamagingAnnotation.polyphenHumdivCCDS, mostDamagingAnnotation.effect);
            } else if (headerIndex == POLYPHEN_HUMVAR_SCORE_HEADER_INDEX) {
                value = FormatManager.getFloat(mostDamagingAnnotation.polyphenHumvar);
            } else if (headerIndex == POLYPHEN_HUMVAR_PREDICTION_HEADER_INDEX) {
                value = PolyphenManager.getPrediction(mostDamagingAnnotation.polyphenHumvar, mostDamagingAnnotation.effect);
            } else if (headerIndex == POLYPHEN_HUMVAR_SCORE_CCDS_HEADER_INDEX) {
                value = FormatManager.getFloat(mostDamagingAnnotation.polyphenHumvarCCDS);
            } else if (headerIndex == POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER_INDEX) {
                value = PolyphenManager.getPrediction(mostDamagingAnnotation.polyphenHumvarCCDS, mostDamagingAnnotation.effect);
            } else if (headerIndex == GENE_NAME_HEADER_INDEX) {
                value = "'" + mostDamagingAnnotation.geneName + "'";
            } else if (headerIndex == ALL_ANNOTATION_HEADER_INDEX) {
                value = allAnnotation;
            } else {
                value = record.get(headerIndex);
            }

            sj.add(value);
        }

        bwGenotypes.write(sj.toString());
        bwGenotypes.newLine();
    }

    private static void initGeneSummaryMap() {
        GeneManager.getMap().values().stream().forEach((geneSet) -> {
            geneSet.stream().forEach((gene) -> {
                if (!summaryMap.containsKey(gene.getName())) {
                    summaryMap.put(gene.getName(), new CollapsingGeneSummary(gene.getName()));
                }
            });
        });
    }

    private static void outputSummary() {
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

    private static void outputMatrix() throws Exception {
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

    private static void generatePvaluesQQPlot() {
        ThirdPartyToolManager.generateQQPlot4CollapsingFetP(summaryFilePath, matrixFilePath, geneFetPQQPlotPath);
    }

    private static void gzipFiles() {
        ThirdPartyToolManager.gzipFile(matrixFilePath);
    }
}
