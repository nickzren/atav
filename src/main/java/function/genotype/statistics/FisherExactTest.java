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
import utils.MathManager;

/**
 *
 * @author nick
 */
public class FisherExactTest extends AnalysisBase4CalledVar {

    String[] originalPOutputPath = new String[StatisticsCommand.fisherModels.length];
    String[] sortedPOutputPath = new String[StatisticsCommand.fisherModels.length];

    BufferedWriter[] originalBw = new BufferedWriter[StatisticsCommand.fisherModels.length];
    BufferedWriter[] sortedBw = new BufferedWriter[StatisticsCommand.fisherModels.length];

    ArrayList<ArrayList<UnsortedOutputData>> unsortedOutputByModelList = new ArrayList<>();

    static int[] qualifiedVarNum = new int[StatisticsCommand.fisherModels.length];

    @Override
    public void initOutput() {
        for (int m = 0; m < StatisticsCommand.fisherModels.length; m++) {
            try {
                String testModel = StatisticsCommand.fisherModels[m];
                originalPOutputPath[m] = CommonCommand.outputPath + testModel + ".csv";
                originalBw[m] = new BufferedWriter(new FileWriter(originalPOutputPath[m]));
                originalBw[m].write(FisherOutput.getTitle());
                originalBw[m].newLine();

                if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                    sortedPOutputPath[m] = CommonCommand.outputPath + testModel
                            + ".p_" + StatisticsCommand.threshold4Sort + ".sorted" + ".csv";
                    sortedBw[m] = new BufferedWriter(new FileWriter(sortedPOutputPath[m]));
                    sortedBw[m].write(FisherOutput.getTitle());
                    sortedBw[m].newLine();

                    unsortedOutputByModelList.add(new ArrayList<>());
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
        for (int m = 0; m < StatisticsCommand.fisherModels.length; m++) {
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
            output.countSampleGeno();
            output.calculate();

            for (int m = 0; m < StatisticsCommand.fisherModels.length; m++) {
                if (output.isValid(StatisticsCommand.fisherModels[m])) {
                    qualifiedVarNum[m]++;

                    ArrayList<Integer> countList = new ArrayList<>();
                    output.initCount(countList, StatisticsCommand.fisherModels[m]);
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

    private void addToListByP(FisherOutput output, int m) {
        try {
            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER
                    && output.pValue <= StatisticsCommand.threshold4Sort) {
                UnsortedOutputData data = new UnsortedOutputData(output, output.pValue);
                unsortedOutputByModelList.get(m).add(data);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputSortedData() {
        try {
            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                for (int m = 0; m < StatisticsCommand.fisherModels.length; m++) {
                    ArrayList<UnsortedOutputData> list = unsortedOutputByModelList.get(m);

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
        for (int m = 0; m < StatisticsCommand.fisherModels.length; m++) {
            LogManager.writeAndPrint("Total qualified " 
                    + StatisticsCommand.fisherModels[m] + " variants: " + qualifiedVarNum[m]);

            double bonferroniP = MathManager.devide(0.05, qualifiedVarNum[m]);

            LogManager.writeAndPrint("Bonferroni correction p-value (" 
                    + StatisticsCommand.fisherModels[m] + "): "
                    + FormatManager.getDouble(bonferroniP));
        }
    }

    private void generatePvaluesQQPlot() {
        for (int m = 0; m < StatisticsCommand.fisherModels.length; m++) {
            ThirdPartyToolManager.generatePvaluesQQPlot(FisherOutput.getTitle(),
                    "P Value",
                    originalPOutputPath[m],
                    originalPOutputPath[m].replace(".csv", ".p.qq.plot.pdf"));

            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                ThirdPartyToolManager.generatePvaluesQQPlot(FisherOutput.getTitle(),
                        "P Value",
                        sortedPOutputPath[m],
                        sortedPOutputPath[m].replace(".csv", ".p.qq.plot.pdf"));
            }
        }
    }

    private void gzipFiles() {
        if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
            for (int m = 0; m < StatisticsCommand.fisherModels.length; m++) {
                ThirdPartyToolManager.gzipFile(originalPOutputPath[m]);
            }
        }
    }

    @Override
    public String toString() {
        return "It is running a Fisher's Exact Test function...";
    }
}
