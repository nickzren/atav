package function.genotype.statistics;

import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import global.Data;
import utils.ErrorManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import utils.CommonCommand;

/**
 *
 * @author nick
 */
public class LinearRegression extends AnalysisBase4CalledVar {

    String[] originalPOutputPath = new String[StatisticsCommand.models.length];
    String[] sortedPOutputPath = new String[StatisticsCommand.models.length];

    BufferedWriter[] linearBw = new BufferedWriter[StatisticsCommand.models.length];
    BufferedWriter[] sortedBw = new BufferedWriter[StatisticsCommand.models.length];

    HashMap<Integer, ArrayList<UnsortedOutputData>> unsortedMap
            = new HashMap<Integer, ArrayList<UnsortedOutputData>>();

    @Override
    public void initOutput() {
        for (int m = 0; m < StatisticsCommand.models.length; m++) {
            try {
                String testModel = StatisticsCommand.models[m];
                originalPOutputPath[m] = CommonCommand.outputPath + testModel + ".csv";
                linearBw[m] = new BufferedWriter(new FileWriter(originalPOutputPath[m]));
                linearBw[m].write(LinearOutput.title);
                linearBw[m].newLine();

                if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                    sortedPOutputPath[m] = CommonCommand.outputPath + testModel
                            + ".p_" + StatisticsCommand.threshold4Sort + ".sorted" + ".csv";
                    sortedBw[m] = new BufferedWriter(new FileWriter(sortedPOutputPath[m]));
                    sortedBw[m].write(LinearOutput.title);
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
                linearBw[m].flush();
                linearBw[m].close();

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
        outputSortedData();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            LinearOutput output = new LinearOutput(calledVar);
            output.countSampleGeno();
            output.calculate();

            for (int m = 0; m < StatisticsCommand.models.length; m++) {
                if (output.isValid(StatisticsCommand.models[m])) {
                    output.doRegression(StatisticsCommand.models[m]);

                    addToListByP(output, m);

                    linearBw[m].write(output.toString());
                    linearBw[m].newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void addToListByP(LinearOutput output, int m) {
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

    private void generatePvaluesQQPlot() {
        for (int m = 0; m < StatisticsCommand.models.length; m++) {
            ThirdPartyToolManager.generatePvaluesQQPlot(LinearOutput.title,
                    "P value",
                    originalPOutputPath[m],
                    originalPOutputPath[m].replace(".csv", ".p.qq.plot.pdf"));

            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
                ThirdPartyToolManager.generatePvaluesQQPlot(LinearOutput.title,
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
        return "It is running a linear regression function...";
    }
}
