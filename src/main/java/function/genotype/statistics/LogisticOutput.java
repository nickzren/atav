package function.genotype.statistics;

import function.annotation.base.GeneManager;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import global.Data;
import global.Index;
import org.renjin.sexp.DoubleVector;
import utils.FormatManager;
import utils.LogManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class LogisticOutput extends StatisticOutput {

    public static final String title
            = "Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "CADD Score Phred,"
            + GerpManager.getTitle()
            + "Is Minor Ref,"
            + "Major Hom Ctrl,"
            + "Het Ctrl,"
            + "Minor Hom Ctrl,"
            + "Minor Hom Ctrl Freq,"
            + "Het Ctrl Freq,"
            + "Missing Ctrl,"
            + "QC Fail Ctrl,"
            + "Ctrl Maf,"
            + "Ctrl HWE_P,"
            + "P value,"
            + "Avg Min Ctrl Cov,"
            + EvsManager.getTitle()
            + "Polyphen Humdiv Score,"
            + "Polyphen Humdiv Prediction,"
            + "Polyphen Humvar Score,"
            + "Polyphen Humvar Prediction,"
            + "Function,"
            + "Gene Name,"
            + "Artifacts in Gene,"
            + "Codon Change,"
            + "Gene Transcript (AA Change),"
            + ExacManager.getTitle()
            + KaviarManager.getTitle()
            + KnownVarManager.getTitle()
            + RvisManager.getTitle()
            + SubRvisManager.getTitle()
            + GenomesManager.getTitle();

    public LogisticOutput(CalledVariant c) {
        super(c);
    }

    public boolean isValid(String model) {
        if (model.equals("recessive")) {
            if (!isRecessive()) {
                return false;
            }
        }

        if (isValid()) {
            return true;
        }

        return false;
    }

    public double doLogisticRegression(List<Double> response, List<List<Double>> covariates ){
        final StringBuilder expression= new StringBuilder("y~");
        int covariantcount=covariates.size();
        ScriptEngineManager manager = new ScriptEngineManager();
        double pval=Data.NA;

        for (int i=0; i< covariantcount; i++){
            expression.append("x").append(i+1);
            if (i!=covariantcount-1) expression.append("+");
        }

        ScriptEngine engine = manager.getEngineByName("Renjin");
        if(engine == null) {
            throw new RuntimeException("Renjin Script Engine not found on the classpath.");
        }


        try{
            String glmExpression="logredmd <-glm("+expression.toString()+", family=\"binomial\" )";
            String fitExpression="with(logredmd, pchisq(null.deviance - deviance, df.null - df.residual, lower.tail=FALSE))";
            String regParam;


            for(int i=1; i<=covariantcount; i++){
                regParam="x"+i;
                engine.put(regParam,covariates.get(i-1));
                engine.eval(regParam+" <- as.numeric(unlist("+regParam+"))");
            }

            engine.put("y", response);
            engine.eval(" y <- as.numeric(unlist(y))");

            LogManager.writeAndPrint("Evaluating "+glmExpression);

            engine.eval(glmExpression);
            DoubleVector res =(DoubleVector) engine.eval(fitExpression);
            pval=(null!=res)?res.getElementAsDouble(0):Data.NA;
        }catch (ScriptException e) {
            e.printStackTrace();
        }

        return pval;
    }

    public void doRegression(String model) {
        int eigencount=SampleManager.getList().get(1).getCovariateList().size();

        if (eigencount<=0){
            pValue = Data.NA;
            return;
        }

        //Initializing Params
        List<Double> response= new ArrayList<>();
        List<List<Double>> covariates = new ArrayList<>();

        for (int i=0; i< eigencount; i++){
            covariates.add(new ArrayList<Double>());
        }

        for (Sample sample : SampleManager.getList()) {
            ArrayList<String> covData = sample.getCovariateList();

            //Set predictors
            for (int i=0; i< eigencount; i++){
                covariates.get(i).add(Double.parseDouble(covData.get(i)));
            }

            //Set REsponse

            int geno = calledVar.getGenotype(sample.getIndex());
            if (geno != Data.NA) {
                if (model.equals("allelic")) {
                    if (isMinorRef) {
                        if (geno == Index.REF) {
                            response.add(1d);
                            response.add(1d);
                        } else if (geno == Index.HET) {
                            response.add(1d);
                            response.add(0d);
                        } else if (geno == Index.HOM) {
                            response.add(0d);
                            response.add(0d);
                        }
                    } else {
                        if (geno == Index.REF) {
                            response.add(0d);
                            response.add(0d);
                        } else if (geno == Index.HET) {
                            response.add(1d);
                            response.add(0d);
                        } else if (geno == Index.HOM) {
                            response.add(1d);
                            response.add(1d);
                        }
                    }
                } else if (model.equals("dominant")) {
                    if (isMinorRef) {
                        if (geno == Index.REF) {
                            response.add(1d);
                        } else if (geno == Index.HET) {
                            response.add(1d);
                        } else if (geno == Index.HOM) {
                            response.add(0d);
                        } else if (geno == Index.HOM_MALE) {
                            response.add(0d);
                        } else if (geno == Index.REF_MALE) {
                            response.add(1d);
                        }
                    } else {
                        if (geno == Index.REF) {
                            response.add(0d);
                        } else if (geno == Index.HET) {
                            response.add(1d);
                        } else if (geno == Index.HOM) {
                            response.add(1d);
                        } else if (geno == Index.HOM_MALE) {
                            response.add(1d);
                        } else if (geno == Index.REF_MALE) {
                            response.add(0d);
                        }
                    }
                } else if (model.equals("recessive")) {
                    if (isMinorRef) {
                        if (geno == Index.REF) {
                            response.add(1d);
                        } else if (geno == Index.HET) {
                            response.add(0d);
                        } else if (geno == Index.HOM) {
                            response.add(0d);
                        } else if (geno == Index.HOM_MALE) {
                            response.add(0d);
                        } else if (geno == Index.REF_MALE) {
                            response.add(1d);
                        }
                    } else {
                        if (geno == Index.REF) {
                            response.add(0d);
                        } else if (geno == Index.HET) {
                            response.add(0d);
                        } else if (geno == Index.HOM) {
                            response.add(1d);
                        } else if (geno == Index.HOM_MALE) {
                            response.add(1d);
                        } else if (geno == Index.REF_MALE) {
                            response.add(0d);
                        }
                    }
                }
            }
            else {
                LogManager.writeAndPrint("model is not recognized");
            }
        }

        pValue = doLogisticRegression(response, covariates);
        LogManager.writeAndPrint("Logistic regression completed successfully with P - Value " + pValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(calledVar.getGerpScore());
        sb.append(isMinorRef).append(",");
        sb.append(majorHomCtrl).append(",");
        sb.append(sampleCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCtrl).append(",");
        sb.append(FormatManager.getDouble(ctrlMhgf)).append(",");
        sb.append(FormatManager.getDouble(sampleFreq[Index.HET][Index.CTRL])).append(",");
        sb.append(sampleCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(ctrlMaf)).append(",");
        sb.append(FormatManager.getDouble(ctrlHweP)).append(",");
        sb.append(FormatManager.getDouble(pValue)).append(",");
        sb.append(FormatManager.getDouble(averageCov[Index.CTRL])).append(",");

        sb.append(calledVar.getEvsStr());

        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(calledVar.getGeneName()))).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");

        sb.append(calledVar.getExacStr());

        sb.append(calledVar.getKaviarStr());

        sb.append(calledVar.getKnownVarStr());

        sb.append(calledVar.getRvis());

        sb.append(calledVar.getSubRvis());

        sb.append(calledVar.get1000Genomes());

        return sb.toString();
    }
}
