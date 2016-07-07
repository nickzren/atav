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
 * @author nick, kaustubh
 */
public class LogisticRegression extends AnalysisBase4CalledVar {

    String origOutPath;
    BufferedWriter logRegBw;

    @Override
    public void initOutput() {
        try {
            origOutPath = CommonCommand.outputPath + "logistic.csv";
            logRegBw = new BufferedWriter(new FileWriter(origOutPath));
            logRegBw.write(LogisticOutput.getTitle());
            logRegBw.newLine();
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
            logRegBw.flush();
            logRegBw.close();
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
        initResponseAndCoviates();
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

            if (output.isValid()) {
                output.initGenoMapAndSampleIndexList();
                output.doRegressionAll();
                logRegBw.write(output.toString());
                logRegBw.newLine();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initResponseAndCoviates() {
        try {
            //Initializing Params
            List<Double> response = new ArrayList<>(); // all sample phenotypes
            List<List<Double>> covariates = new ArrayList<>(); // all sample covariates per column per list

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

    private void generatePvaluesQQPlot() {
            ThirdPartyToolManager.generatePvaluesQQPlot(
                    LogisticOutput.getTitle(),
                    "Dominant P Value",
                    origOutPath,
                    origOutPath.replace(".csv", "." + "dominant.p.qq.plot.pdf"));

            ThirdPartyToolManager.generatePvaluesQQPlot(
                    LogisticOutput.getTitle(),
                    "Recessive P Value",
                    origOutPath,
                    origOutPath.replace(".csv", "." + "recessive.p.qq.plot.pdf"));

            ThirdPartyToolManager.generatePvaluesQQPlot(
                    LogisticOutput.getTitle(),
                    "Additive P Value",
                    origOutPath,
                    origOutPath.replace(".csv", "." + "additive.p.qq.plot.pdf"));
    }

    @Override
    public String toString() {
        return "It is running a logistic regression function...";
    }
}
