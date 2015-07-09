package function.genotype.collapsing;

import function.genotype.base.Sample;
import function.genotype.statistics.FisherExact;
import global.Data;
import function.annotation.base.GeneManager;
import function.annotation.base.IntolerantScoreManager;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.FormatManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 *
 * @author nick
 */
public class CollapsingSummary implements Comparable {

    int[] variantNumBySample = new int[SampleManager.getListSize()];
    String geneName;
    int totalVariant = 0;
    int totalSnv = 0;
    int totalIndel = 0;
    // a b c d will be passed to calculated fisher p
    int qualifiedCase = 0;  // a
    int unqualifiedCase = 0; // b
    int qualifiedCtrl = 0;   // c
    int unqualifiedCtrl = 0; // d 
    double qualifiedCaseFreq = Data.NA;
    double qualifiedCtrlFreq = Data.NA;
    String enrichedDirection = "NA";
    double fetP = Data.NA;
    double logisticP = Data.NA;
    double linearP = Data.NA;
    String coverageSummaryLine;

    // no output
    static final int totalCase = SampleManager.getCaseNum();
    static final int totalCtrl = SampleManager.getCtrlNum();

    // output columns 
    public static final String title
            = "Rank,"
            + "Gene Name,"
            + IntolerantScoreManager.getTitle()
            + "Artifacts in Gene,"
            + "Total Variant,"
            + "Total SNV,"
            + "Total Indel,"
            + "Qualified Case,"
            + "Unqualified Case,"
            + "Qualified Case Freq,"
            + "Qualified Ctrl,"
            + "Unqualified Ctrl,"
            + "Qualified Ctrl Freq,"
            + "Enriched Direction,"
            + "Fet P,"
            + "Linear P,"
            + "Logistic P,"
            + GeneManager.getCoverageSummary("title")
            + "\n";

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("'").append(geneName).append("'").append(",");
        sb.append(IntolerantScoreManager.getValues(geneName)).append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(geneName))).append(",");
        sb.append(totalVariant).append(",");
        sb.append(totalSnv).append(",");
        sb.append(totalIndel).append(",");
        sb.append(qualifiedCase).append(",");
        sb.append(unqualifiedCase).append(",");
        sb.append(FormatManager.getDouble(qualifiedCaseFreq)).append(",");
        sb.append(qualifiedCtrl).append(",");
        sb.append(unqualifiedCtrl).append(",");
        sb.append(FormatManager.getDouble(qualifiedCtrlFreq)).append(",");
        sb.append(enrichedDirection).append(",");
        sb.append(FormatManager.getDouble(fetP)).append(",");
        sb.append(FormatManager.getDouble(linearP)).append(",");
        sb.append(FormatManager.getDouble(logisticP)).append(",");
        sb.append(GeneManager.getCoverageSummary(geneName));

        return sb.toString();
    }

    public CollapsingSummary(String name) {
        geneName = name;
    }

    public void updateSampleVariantCount4SingleVar(int index) {
        variantNumBySample[index] = variantNumBySample[index] + 1;
    }

    public void updateSampleVariantCount4CompHet(int index) {
        if (variantNumBySample[index] == 0) {
            variantNumBySample[index] = variantNumBySample[index] + 1;
        }
    }

    public void updateVariantCount(CollapsingOutput output) {
        totalVariant++;

        if (output.getCalledVariant().isSnv()) {
            totalSnv++;
        } else {
            totalIndel++;
        }
    }

    public void updateVariantCount(CompHetOutput output) {
        totalVariant++;

        if (output.getCalledVariant().isSnv()) {
            totalSnv++;
        } else {
            totalIndel++;
        }
    }

    public void countSample() {
        for (int s = 0; s < SampleManager.getListSize(); s++) {
            Sample sample = SampleManager.getList().get(s);
            if (sample.isCase()) {
                if (variantNumBySample[s] > 0) {
                    qualifiedCase++;
                }
            } else {
                if (variantNumBySample[s] > 0) {
                    qualifiedCtrl++;
                }
            }
        }

        unqualifiedCase = totalCase - qualifiedCase;
        unqualifiedCtrl = totalCtrl - qualifiedCtrl;

        qualifiedCaseFreq = FormatManager.devide(qualifiedCase, totalCase);
        qualifiedCtrlFreq = FormatManager.devide(qualifiedCtrl, totalCtrl);

        if (qualifiedCaseFreq == 0
                && qualifiedCtrlFreq == 0) {
            enrichedDirection = "NA";
        } else if (qualifiedCaseFreq == qualifiedCtrlFreq) {
            enrichedDirection = "none";
        } else if (qualifiedCaseFreq < qualifiedCtrlFreq) {
            enrichedDirection = "ctrl";
        } else if (qualifiedCaseFreq > qualifiedCtrlFreq) {
            enrichedDirection = "case";
        }
    }

    public void calculateFetP() {
        fetP = FisherExact.getTwoTailedP(qualifiedCase, unqualifiedCase,
                qualifiedCtrl, unqualifiedCtrl);
    }

    public double getFetP() {
        return fetP;
    }

    public void setLogisticP(double value) {
        logisticP = value;
    }

    public void setLinearP(double value) {
        linearP = value;
    }

    public static void calculateLinearAndLogisticP(String geneSampleMatrixFilePath,
            Hashtable<String, CollapsingSummary> summaryTable) throws Exception {
        String geneLogisticPPath = CommonCommand.outputPath + "gene.logistic.p.csv";
        String geneLinearPPath = CommonCommand.outputPath + "gene.linear.p.csv";

        if (CollapsingCommand.isCollapsingDoLogistic) {
            calculateRegression(geneSampleMatrixFilePath,
                    summaryTable, geneLogisticPPath, "logistf");
        } else if (CollapsingCommand.isCollapsingDoLinear) {
            calculateRegression(geneSampleMatrixFilePath,
                    summaryTable, geneLinearPPath, "linear");
        }
    }

    private static void calculateRegression(String geneSampleMatrixFilePath,
            Hashtable<String, CollapsingSummary> summaryTable,
            String outputFile, String method) throws Exception {
        ThirdPartyToolManager.callCollapsedRegression(outputFile,
                geneSampleMatrixFilePath, method);

        File f = new File(outputFile);

        if (f.exists()) {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;

            br.readLine(); // ingore title

            while ((line = br.readLine()) != null) {
                String[] temp = line.split(",");

                String geneName = temp[0].replaceAll("'", "");

                if (method.equals("logistf")) {
                    summaryTable.get(geneName).setLogisticP(Double.valueOf(temp[1]));
                } else { // linear
                    summaryTable.get(geneName).setLinearP(Double.valueOf(temp[1]));
                }
            }
        }
    }

    public int compareTo(Object another) throws ClassCastException {
        CollapsingSummary that = (CollapsingSummary) another;
        return Double.compare(this.fetP, that.fetP); //small -> large
    }
}
