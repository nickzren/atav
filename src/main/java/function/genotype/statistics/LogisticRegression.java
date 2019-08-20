//package function.genotype.statistics;
//
//import function.genotype.base.AnalysisBase4CalledVar;
//import function.genotype.base.CalledVariant;
//import function.genotype.base.Sample;
//import function.genotype.base.SampleManager;
//import global.Data;
//import utils.CommonCommand;
//import utils.ErrorManager;
//import utils.MathManager;
//import utils.ThirdPartyToolManager;
//
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.util.*;
//
///**
// *
// * @author nick, kaustubh
// */
//public class LogisticRegression extends AnalysisBase4CalledVar {
//
//    String origOutPath;
//    String[] sortedPOutPath = new String[StatisticsCommand.logisticModels.length];
//
//    BufferedWriter logisticBw;
//    BufferedWriter[] sortedBw = new BufferedWriter[StatisticsCommand.logisticModels.length];
//
//    ArrayList<ArrayList<UnsortedOutputData>> unsortedOutputByModelList = new ArrayList<>();
//
//    @Override
//    public void initOutput() {
//        try {
//            origOutPath = CommonCommand.outputPath + "logistic.csv";
//            logisticBw = new BufferedWriter(new FileWriter(origOutPath));
//            logisticBw.write(LogisticOutput.getTitle());
//            logisticBw.newLine();
//
//            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
//                for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
//                    String model = StatisticsCommand.logisticModels[m];
//                    sortedPOutPath[m] = CommonCommand.outputPath + model
//                            + ".p_" + StatisticsCommand.threshold4Sort + ".sorted" + ".csv";
//                    sortedBw[m] = new BufferedWriter(new FileWriter(sortedPOutPath[m]));
//                    sortedBw[m].write(LogisticOutput.getTitle());
//                    sortedBw[m].newLine();
//
//                    unsortedOutputByModelList.add(new ArrayList<>());
//                }
//            }
//        } catch (Exception ex) {
//            ErrorManager.send(ex);
//        }
//    }
//
//    @Override
//    public void doOutput() {
//    }
//
//    @Override
//    public void closeOutput() {
//        try {
//            logisticBw.flush();
//            logisticBw.close();
//
//            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
//                for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
//                    sortedBw[m].flush();
//                    sortedBw[m].close();
//                }
//            }
//        } catch (Exception ex) {
//            ErrorManager.send(ex);
//        }
//    }
//
//    @Override
//    public void doAfterCloseOutput() {
//        generatePvaluesQQPlot();
//        
//        gzipFiles();
//    }
//
//    @Override
//    public void beforeProcessDatabaseData() {
//        initResponseAndCoviates();
//    }
//
//    @Override
//    public void afterProcessDatabaseData() {
//        outputSortedData();
//    }
//
//    @Override
//    public void processVariant(CalledVariant calledVar) {
//        try {
//            LogisticOutput output = new LogisticOutput(calledVar);
//            output.countSampleGeno();
//            output.calculate();
//
//            if (output.isValid()) {
//                output.initGenoMapAndSampleIndexList();
//                output.doRegressionAll();
//                logisticBw.write(output.toString());
//                logisticBw.newLine();
//
//                addToListByP(output);
//            }
//        } catch (Exception e) {
//            ErrorManager.send(e);
//        }
//    }
//
//    private void addToListByP(LogisticOutput output) {
//        try {
//            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
//                for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
//                    if (output.pValues[m] <= StatisticsCommand.threshold4Sort) {
//                        UnsortedOutputData data = new UnsortedOutputData(output, output.pValues[m]);
//                        unsortedOutputByModelList.get(m).add(data);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            ErrorManager.send(e);
//        }
//    }
//
//    private void outputSortedData() {
//        try {
//            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
//                for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
//                    ArrayList<UnsortedOutputData> list = unsortedOutputByModelList.get(m);
//
//                    Collections.sort(list);
//
//                    for (UnsortedOutputData output : list) {
//                        sortedBw[m].write(output.line);
//                        sortedBw[m].newLine();
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            ErrorManager.send(e);
//        }
//    }
//
//    private void initResponseAndCoviates() {
//        try {
//            //Initializing Params
//            List<Double> response = new ArrayList<>(); // all sample phenotypes
//            List<List<Double>> covariates = new ArrayList<>(); // all sample covariates per column per list
//
//            for (int i = 0; i < SampleManager.getCovariateNum(); i++) {
//                covariates.add(new ArrayList<>());
//            }
//
//            //Get Data for response and covariates
//            for (Sample sample : SampleManager.getList()) {
//                response.add((double) sample.getPheno());
//
//                //Set other predictors
//                for (int i = 0; i < SampleManager.getCovariateNum(); i++) {
//                    covariates.get(i).add(sample.getCovariateList().get(i));
//                }
//            }
//
//            //Set covariate data in Renjin
//            String regParam;
//            for (int i = 1; i <= SampleManager.getCovariateNum(); i++) {
//                regParam = "x" + i;
//                MathManager.getRenjinEngine().put(regParam, covariates.get(i - 1));
//                MathManager.getRenjinEngine().eval(regParam + " <- as.numeric(unlist(" + regParam + "))");
//            }
//
//            //Set response data in Renjin
//            MathManager.getRenjinEngine().put("y", response);
//            MathManager.getRenjinEngine().eval(" y <- as.numeric(unlist(y))");
//
//            //Initialize expression for covariates plus genotype
//            LogisticOutput.initExpression();
//        } catch (Exception e) {
//            ErrorManager.send(e);
//        }
//    }
//
//    private void generatePvaluesQQPlot() {
//        for (int m = 0; m < StatisticsCommand.logisticModels.length; m++) {
//            String model = StatisticsCommand.logisticModels[m];
//
//            ThirdPartyToolManager.generatePvaluesQQPlot(LinearOutput.getTitle(),
//                    model + " P Value",
//                    origOutPath,
//                    origOutPath.replace(".csv", "." + model + ".p.qq.plot.pdf"));
//
//            if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
//                ThirdPartyToolManager.generatePvaluesQQPlot(LinearOutput.getTitle(),
//                        model + " P Value",
//                        sortedPOutPath[m],
//                        sortedPOutPath[m].replace(".csv", ".p.qq.plot.pdf"));
//            }
//        }
//    }
//
//    private void gzipFiles() {
//        if (StatisticsCommand.threshold4Sort != Data.NO_FILTER) {
//            ThirdPartyToolManager.gzipFile(origOutPath);
//        }
//    }
//
//    @Override
//    public String toString() {
//        return "Start running logistic regression function";
//    }
//}
