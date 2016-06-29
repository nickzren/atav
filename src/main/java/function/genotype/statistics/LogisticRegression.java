package function.genotype.statistics;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.MathManager;
import utils.ThirdPartyToolManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 *
 * @author nick
 */
public class LogisticRegression extends AnalysisBase4CalledVar {


    /**Need Model list to be sorted
     * **/

    String origOutPath;
    BufferedWriter logregBw;
    Map<String,String> ModelHeaderMap;

    @Override
    public void initOutput() {

        try{
            StatisticsCommand.sortLogisticModels();
            String header;
            ModelHeaderMap= new HashMap<>();
                for (String s: StatisticsCommand.logisticModels){
                    header=s+" P Value";
                    ModelHeaderMap.put(s,header);
                }
            origOutPath = CommonCommand.outputPath + "master.csv";
            logregBw = new BufferedWriter(new FileWriter(origOutPath));
            logregBw.write(LogisticOutput.getTitle());
            logregBw.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {



        try {
            logregBw.flush();
            logregBw.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        generatePvaluesQQPlot();
    }

    @Override
    public void beforeProcessDatabaseData() {
        try {
            //Initializing Params
            List<Double> response = new ArrayList<>(); // all sample phenotypes
            List<List<Double>> covariates = new ArrayList<>(); // all sample covariates per column per list

            initResponseAndCoviates(response, covariates);

            //Set covariate data in Renjin
            String regParam;
            for (int i = 1; i <= SampleManager.getCovariateNum(); i++) {
                regParam = "x" + i;
                MathManager.getRenjinEngine().put(regParam, covariates.get(i - 1));
                MathManager.getRenjinEngine().eval(regParam + " <- as.numeric(unlist(" + regParam + "))");
            }

            //Set response data in Renjin
            MathManager.getRenjinEngine().put("y", response);
            MathManager.getRenjinEngine().eval(" y <- as.numeric(unlist(y))");

            //Initialize expression for covariates plus genotype
            LogisticOutput.initExpression();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initResponseAndCoviates(List<Double> response,
            List<List<Double>> covariates) {
        for (int i = 0; i < SampleManager.getCovariateNum(); i++) {
            covariates.add(new ArrayList<>());
        }

        //Get Data for response and covariates
        for (Sample sample : SampleManager.getList()) {
            response.add((double) sample.getPheno());

            //Set other predictors
            for (int i = 0; i < SampleManager.getCovariateNum(); i++) {
                covariates.get(i).add(sample.getCovariateList().get(i));
            }
        }
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            LogisticOutput output = new LogisticOutput(calledVar);
            output.countSampleGeno();
            output.calculate();
            
            //initialize genotypes for all models
            output.initGenotypeAndSampleIndexList(StatisticsCommand.logisticModels);

            if (output.isValid()) {
                output.doRegressionAll();
                logregBw.write(output.toString());
                logregBw.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void generatePvaluesQQPlot() {
        for (String s: StatisticsCommand.logisticModels){
            String header=ModelHeaderMap.get(s);
            ThirdPartyToolManager.generatePvaluesQQPlot(LogisticOutput.getTitle(),
                    header,
                    origOutPath,
                    origOutPath.replace(".csv", s+".p.qq.plot.pdf"));
        }


    }

    @Override
    public String toString() {
        return "It is running a logistic regression function...";
    }
}
