package function.genotype.collapsing;

import function.annotation.base.GeneManager;
import utils.CommonCommand;
import utils.FormatManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class CollapsingGeneSummary extends CollapsingSummary {

    // output columns 
    public static String getTitle() {
        return "Rank,"
                + "Gene Name,"
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
                + GeneManager.getCoverageSummary("title");
    }

    String coverageSummaryLine;

    public CollapsingGeneSummary(String name) {
        super(name);
    }

    public static void calculateLinearAndLogisticP(String geneSampleMatrixFilePath,
            HashMap<String, CollapsingSummary> summaryMap) throws Exception {
        String geneLogisticPPath = CommonCommand.outputPath + "gene.logistic.p.csv";
        String geneLinearPPath = CommonCommand.outputPath + "gene.linear.p.csv";

        if (CollapsingCommand.isCollapsingDoLogistic) {
            calculateRegression(geneSampleMatrixFilePath,
                    summaryMap, geneLogisticPPath, "logistf");
        } else if (CollapsingCommand.isCollapsingDoLinear) {
            calculateRegression(geneSampleMatrixFilePath,
                    summaryMap, geneLinearPPath, "linear");
        }
    }

    private static void calculateRegression(String geneSampleMatrixFilePath,
            HashMap<String, CollapsingSummary> summaryMap,
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
                    summaryMap.get(geneName).setLogisticP(Double.valueOf(temp[1]));
                } else { // linear
                    summaryMap.get(geneName).setLinearP(Double.valueOf(temp[1]));
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("'").append(name).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(name))).append(",");
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
        sb.append(GeneManager.getCoverageSummary(name));

        return sb.toString();
    }
}
