package function.genotype.statistics;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.MathManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class LogisticRegression extends AnalysisBase4CalledVar {

    String[] originalPOutputPath = new String[StatisticsCommand.logisticModels.length];
    BufferedWriter[] logisticBw = new BufferedWriter[StatisticsCommand.logisticModels.length];

    public int getEigenCount() {
        return eigenCount;
    }

    public void setEigenCount(int eigenCount) {
        this.eigenCount = eigenCount;
    }

    public int eigenCount;

    @Override
    public void initOutput() {
        for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
            try {
                String testModel = StatisticsCommand.logisticModels[m];
                originalPOutputPath[m] = CommonCommand.outputPath + testModel + ".csv";
                logisticBw[m] = new BufferedWriter(new FileWriter(originalPOutputPath[m]));
                logisticBw[m].write(LogisticOutput.title);
                logisticBw[m].newLine();
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
        for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
            try {
                logisticBw[m].flush();
                logisticBw[m].close();
            } catch (Exception ex) {
                ErrorManager.send(ex);
            }
        }
    }

    @Override
    public void doAfterCloseOutput() {
        generatePvaluesQQPlot();
    }

    @Override
    public void beforeProcessDatabaseData() {
        //Initializing Params
        List<Double> response = SampleManager.getList().stream().map(p -> (double) p.getPheno()).collect(Collectors.toList());
        List<List<Double>> covariates = new ArrayList<>(); // all sample covariates per column per list

        setEigenCount(SampleManager.getList().get(0).getCovariateList().size());
        for (int i = 0; i < eigenCount; i++) {
            covariates.add(new ArrayList<>());
        }

        //Get Data for response and covariates
        for (Sample sample : SampleManager.getList()) {
            response.add((double) sample.getPheno());
            //Set other predictors
            for (int i = 0; i < eigenCount; i++) {
                covariates.get(i).add(sample.getCovariateList().get(i));
            }
        }
        //Set master data in Renjin - R env ; Hope this works :)
        try{
            //Set covariate data in Renjin
            String regParam;
            for (int i = 1; i <= eigenCount; i++) {
                regParam = "x" + i;
                MathManager.getRenjinEngine().put(regParam, covariates.get(i - 1));
                MathManager.getRenjinEngine().eval(regParam + " <- as.numeric(unlist(" + regParam + "))");
            }
            //Set response data in Renjin
            MathManager.getRenjinEngine().put("y", response);
            MathManager.getRenjinEngine().eval(" y <- as.numeric(unlist(y))");



        }catch (Exception e){
            ErrorManager.send(e);
        }





    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            LogisticOutput output = new LogisticOutput(calledVar, getEigenCount());
            output.countSampleGenoCov();
            output.calculate();

            for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
                if (output.isValid(StatisticsCommand.logisticModels[m])) {
                    // needs to calculate logistic p below
                    output.doRegression(StatisticsCommand.logisticModels[m]);
                    logisticBw[m].write(output.toString());
                    logisticBw[m].newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void generatePvaluesQQPlot() {
        for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
            ThirdPartyToolManager.generatePvaluesQQPlot(LogisticOutput.title,
                    "P value",
                    originalPOutputPath[m],
                    originalPOutputPath[m].replace(".csv", ".p.qq.plot.pdf"));
        }
    }
    
    @Override
    public String toString() {
        return "It is running a logistic regression function...";
    }
}
