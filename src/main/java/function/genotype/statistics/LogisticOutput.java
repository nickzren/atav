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

    int eigencount;
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

    public LogisticOutput(CalledVariant c, int n) {
        super(c);
        eigencount=n;
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

    private double doLogisticRegression( List<Double> genotypicInfo , List<Integer> indices) {
        if (genotypicInfo.size() <= 1) {
            return Data.NA;
        }


        try {
            //Put indices
            MathManager.getRenjinEngine().put("ind", indices);
            MathManager.getRenjinEngine().eval(" ind <- as.numeric(unlist(ind))");

            //Getting genotype info
            MathManager.getRenjinEngine().put("xt1", genotypicInfo);
            MathManager.getRenjinEngine().eval(" xt1 <- as.numeric(unlist(xt1))");

            //Getting covariate subset
            for (int i=1; i<=eigencount; i++){
                MathManager.getRenjinEngine().eval("xt"+(i+1)+"<- x"+i+"[ind+1]");
            }

            //Getting response subset
            MathManager.getRenjinEngine().eval("yt <- y[ind+1]");

            //Evaluating regression
            MathManager.getRenjinEngine().eval("logredmd <-glm(" + expression.toString() + ", family=\"binomial\" )");

            //Getting P value for genotype
            DoubleVector res = (DoubleVector) MathManager.getRenjinEngine().eval("summary(logredmd)$coefficients[2,4]");


            return (null != res) ? res.getElementAsDouble(0) : Data.NA;
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.NA;
    }

    private static void initExpression(int covariantCount) {
        if (expression.length() == 0) {
            expression.append("yt~");

            for (int i = 0; i < covariantCount; i++) {
                expression.append("xt").append(i + 1);
                if (i != covariantCount - 1) {
                    expression.append("+");
                }
            }
        }
    }

    public void doRegression(String model) {
        List<Double> genotypicInfo = new ArrayList<>();
        List<Integer> qualifiedIndices= new ArrayList<>();
        /*int eigenCount = SampleManager.getList().get(0).getCovariateList().size();*/

        for (Sample sample : SampleManager.getList()) {

            //get genotype
            int geno = calledVar.getGenotype(sample.getIndex());
            //set genotypicInfo
            if (geno != Data.NA) {
                if (model.equals("dominant")) {
                    // Index Qualified
                    qualifiedIndices.add(sample.getIndex());
                    if (isMinorRef) {
                        if (geno == Index.REF || geno == Index.HET || geno == Index.REF_MALE) {
                            genotypicInfo.add(1d);
                        } else if (geno == Index.HOM || geno == Index.HOM_MALE) {
                            genotypicInfo.add(0d);
                        }
                    } else {
                        if (geno == Index.REF || geno == Index.REF_MALE) {
                            genotypicInfo.add(0d);
                        } else if (geno == Index.HET || geno == Index.HOM || geno == Index.HOM_MALE) {
                            genotypicInfo.add(1d);
                        }
                    }
                }
            }


        }

        //Initialize expression for covariates plus genotype
        initExpression(eigencount+1);

        pValue = doLogisticRegression(genotypicInfo, qualifiedIndices);
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
