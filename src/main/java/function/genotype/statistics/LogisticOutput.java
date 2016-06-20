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
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import utils.FormatManager;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import org.renjin.sexp.SEXP;
import utils.ErrorManager;
import utils.MathManager;

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

    private static final StringBuilder expression = new StringBuilder();

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

    private double doLogisticRegression(List<Double> response, List<List<Double>> covariates) {
        if (response.size() <= 1) {
            return Data.NA;
        }

        initExpression(covariates.size());

        try {
            String regParam;

            for (int i = 1; i <= covariates.size(); i++) {
                regParam = "x" + i;
                MathManager.getRenjinEngine().put(regParam, covariates.get(i - 1));
                MathManager.getRenjinEngine().eval(regParam + " <- as.numeric(unlist(" + regParam + "))");
            }

            MathManager.getRenjinEngine().put("y", response);
            MathManager.getRenjinEngine().eval(" y <- as.numeric(unlist(y))");

            System.out.println("y= " + ((SEXP) MathManager.getRenjinEngine().eval("length(y)")).asReal());
            System.out.println("x1= " + ((SEXP) MathManager.getRenjinEngine().eval("length(x1)")).asReal());
            System.out.println("x2= " + ((SEXP) MathManager.getRenjinEngine().eval("length(x2)")).asReal());

            MathManager.getRenjinEngine().eval("logredmd <-glm(" + expression.toString() + ", family=\"binomial\" )");

            //String fitExpression = "with(logredmd, pchisq(null.deviance - deviance, df.null - df.residual, lower.tail=FALSE))";
            String extractPval = "summary(logredmd)$coefficients[2,4]";
            DoubleVector res = (DoubleVector) MathManager.getRenjinEngine().eval(extractPval);
            return (null != res) ? res.getElementAsDouble(0) : Data.NA;
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.NA;
    }

    private static void initExpression(int covariantCount) {
        if (expression.length() == 0) {
            expression.append("y~");

            for (int i = 0; i < covariantCount; i++) {
                expression.append("x").append(i + 1);
                if (i != covariantCount - 1) {
                    expression.append("+");
                }
            }
        }
    }

    public void doRegression(String model) {
        int eigenCount = SampleManager.getList().get(0).getCovariateList().size();
        //One more covariate for genotype
        eigenCount++;

        //Initializing Params
        List<Double> response = new ArrayList<>();

        List<List<Double>> covariates = new ArrayList<>(); // all sample covariates per column per list

        //init X for log reg
        for (int i = 0; i < eigenCount; i++) {
            covariates.add(new ArrayList<>());
        }

        for (Sample sample : SampleManager.getList()) {

            //get genotype
            int geno = calledVar.getGenotype(sample.getIndex());

            if (geno != Data.NA) {
                if (model.equals("dominant")) {
                    if (isMinorRef) {
                        if (geno == Index.REF || geno == Index.HET || geno == Index.REF_MALE) {
                            covariates.get(0).add(1d);
                        } else if (geno == Index.HOM || geno == Index.HOM_MALE) {
                            covariates.get(0).add(0d);
                        }
                    } else if (geno == Index.REF || geno == Index.REF_MALE) {
                        covariates.get(0).add(0d);
                    } else if (geno == Index.HET || geno == Index.HOM || geno == Index.HOM_MALE) {
                        covariates.get(0).add(1d);
                    }

                    //Set other predictors
                    for (int i = 1; i < eigenCount; i++) {
                        covariates.get(i).add(sample.getCovariateList().get(i - 1));
                    }

                    //Set Response
                    response.add((double) sample.getPheno());
                }
            }
        }

        pValue = doLogisticRegression(response, covariates);
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
