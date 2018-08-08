package function.genotype.collapsing;

import function.annotation.base.GeneManager;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import utils.CommonCommand;
import utils.FormatManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class CollapsingGeneSummary extends CollapsingSummary {

    // output columns 
    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Rank");
        sj.add("Gene Name");
        sj.add("Total Variant");
        sj.add("Total SNV");
        sj.add("Total Indel");
        sj.add("Qualified Case");
        sj.add("Unqualified Case");
        sj.add("Qualified Case Freq");
        sj.add("Qualified Ctrl");
        sj.add("Unqualified Ctrl");
        sj.add("Qualified Ctrl Freq");
        sj.add("Enriched Direction");
        sj.add("Fet P");
        sj.add("Linear P");
        sj.add("Logistic P");
        if (!GeneManager.geneCoverageSummaryTitle.isEmpty()) {
            sj.add(GeneManager.geneCoverageSummaryTitle);
        }
        if (RvisCommand.isIncludeRvis) {
            sj.add(RvisManager.getTitle());
        }

        return sj.toString();
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

    public String getRvis() {
        String geneName = name;

        if (name.contains("_")) { // if using gene domain
            geneName = name.substring(0, name.indexOf("_"));
        }

        return RvisManager.getLine(geneName);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("'" + name + "'");
        sj.add(FormatManager.getInteger(totalVariant));
        sj.add(FormatManager.getInteger(totalSnv));
        sj.add(FormatManager.getInteger(totalIndel));
        sj.add(FormatManager.getInteger(qualifiedCase));
        sj.add(FormatManager.getInteger(unqualifiedCase));
        sj.add(FormatManager.getFloat(qualifiedCaseFreq));
        sj.add(FormatManager.getInteger(qualifiedCtrl));
        sj.add(FormatManager.getInteger(unqualifiedCtrl));
        sj.add(FormatManager.getFloat(qualifiedCtrlFreq));
        sj.add(enrichedDirection);
        sj.add(FormatManager.getDouble(fetP));
        sj.add(FormatManager.getDouble(linearP));
        sj.add(FormatManager.getDouble(logisticP));
        GeneManager.addCoverageSummary(name, sj);
        if (RvisCommand.isIncludeRvis) {
            sj.add(getRvis());
        }

        return sj.toString();
    }
}
