package function.genotype.statistics;

import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class FisherExactTest extends AnalysisBase4CalledVar {

    String[] originalPOutputPath = new String[StatisticsCommand.models.length];
    String[] sortedPOutputPath = new String[StatisticsCommand.models.length];

    BufferedWriter[] originalBw = new BufferedWriter[StatisticsCommand.models.length];
    BufferedWriter[] sortedBw = new BufferedWriter[StatisticsCommand.models.length];

    HashMap<Integer, ArrayList<UnsortedOutputData>> unsortedMap
            = new HashMap<Integer, ArrayList<UnsortedOutputData>>();

    static int qualifiedDomVarNum = 0;
    static int qualifiedAleVarNum = 0;
    static int qualifiedGenVarNum = 0;
    static int qualifiedRecVarNum = 0;

    @Override
    public void initOutput() {
        for (int m = 0; m < StatisticsCommand.models.length; m++) {
            try {
                String testModel = StatisticsCommand.models[m];
                originalPOutputPath[m] = CommonCommand.outputPath + testModel + ".csv";
                originalBw[m] = new BufferedWriter(new FileWriter(originalPOutputPath[m]));
                originalBw[m].write(FisherOutput.title);
                originalBw[m].newLine();

                if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                    sortedPOutputPath[m] = CommonCommand.outputPath + testModel
                            + ".p_" + StatisticsCommand.threshold4Sort + ".sorted" + ".csv";
                    sortedBw[m] = new BufferedWriter(new FileWriter(sortedPOutputPath[m]));
                    sortedBw[m].write(FisherOutput.title);
                    sortedBw[m].newLine();

                    unsortedMap.put(m, new ArrayList<UnsortedOutputData>());
                }
            } catch (Exception ex) {
                ErrorManager.send(ex);
            }
        }
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {
        for (int m = 0; m < StatisticsCommand.models.length; m++) {
            try {
                originalBw[m].flush();
                originalBw[m].close();

                if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                    sortedBw[m].flush();
                    sortedBw[m].close();
                }
            } catch (Exception ex) {
                ErrorManager.send(ex);
            }
        }
    }

    @Override
    public void doAfterCloseOutput() {
        generatePvaluesQQPlot();
        
        gzipFiles();
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
        outputBonferroni();

        outputSortedData();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            FisherOutput output = new FisherOutput(calledVar);
            output.countSampleGenoCov();
            output.calculate();

            for (int m = 0; m < StatisticsCommand.models.length; m++) {
                if (output.isValid(StatisticsCommand.models[m])) {
                    countVarNum(StatisticsCommand.models[m]);

                    ArrayList<Integer> countList = new ArrayList<Integer>();
                    output.initCount(countList, StatisticsCommand.models[m]);
                    output.calculateP(countList);

                    addToListByP(output, m);

                    originalBw[m].write(output.toString());
                    originalBw[m].newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void countVarNum(String model) {
        if (model.equals("dominant")) {
            qualifiedDomVarNum++;
        } else if (model.equals("allelic")) {
            qualifiedAleVarNum++;
        } else if (model.equals("genotypic")) {
            qualifiedGenVarNum++;
        } else if (model.equals("recessive")) {
            qualifiedRecVarNum++;
        }
    }

    private void addToListByP(FisherOutput output, int m) {
        try {
            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER
                    && output.pValue <= StatisticsCommand.threshold4Sort) {
                UnsortedOutputData data = new UnsortedOutputData(output);
                unsortedMap.get(m).add(data);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputSortedData() {
        try {
            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                for (int m = 0; m < StatisticsCommand.models.length; m++) {
                    ArrayList<UnsortedOutputData> list = unsortedMap.get(m);

                    Collections.sort(list);

                    for (UnsortedOutputData output : list) {
                        sortedBw[m].write(output.line);
                        sortedBw[m].newLine();
                    }
                }
            }

        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputBonferroni() {
        for (String model : StatisticsCommand.models) {
            printBonferroni(model);
        }
    }

    private void printBonferroni(String model) {
        int num = 0;

        if (model.equals("dominant")) {
            num = qualifiedDomVarNum;
        } else if (model.equals("allelic")) {
            num = qualifiedAleVarNum;
        } else if (model.equals("genotypic")) {
            num = qualifiedGenVarNum;
        } else if (model.equals("recessive")) {
            num = qualifiedRecVarNum;
        }

        LogManager.writeAndPrint("Total qualified " + model + " variants: " + num);

        double bonferroniP = MathManager.devide(0.05, num);

        LogManager.writeAndPrint("Bonferroni correction p-value (" + model + "): "
                + FormatManager.getDouble(bonferroniP));
    }

    private void generatePvaluesQQPlot() {
        for (int m = 0; m < StatisticsCommand.models.length; m++) {
            ThirdPartyToolManager.generatePvaluesQQPlot(FisherOutput.title,
                    "P value",
                    originalPOutputPath[m],
                    originalPOutputPath[m].replace(".csv", ".p.qq.plot.pdf"));

            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                ThirdPartyToolManager.generatePvaluesQQPlot(FisherOutput.title,
                        "P value",
                        sortedPOutputPath[m],
                        sortedPOutputPath[m].replace(".csv", ".p.qq.plot.pdf"));
            }
        }
    }
    
    private void gzipFiles() {
        if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
            for (int m = 0; m < StatisticsCommand.models.length; m++) {
                ThirdPartyToolManager.gzipFile(originalPOutputPath[m]);
            }
        }
    }

    @Override
    public String toString() {
        return "It is running a Fisher's Exact Test function...";
    }
}
