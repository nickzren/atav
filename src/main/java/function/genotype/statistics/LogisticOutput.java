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

import java.util.ArrayList;
import java.util.List;
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

    public void doRegression(String model) {
        List<Double> genotypeList = new ArrayList<>();
        List<Integer> sampleIndexList = new ArrayList<>();

        initGenotypeAndSampleIndexList(model, genotypeList, sampleIndexList);

        pValue = getPValue(genotypeList, sampleIndexList);
    }

    private void initGenotypeAndSampleIndexList(String model,
            List<Double> genotypeList,
            List<Integer> sampleIndexList) {
        for (Sample sample : SampleManager.getList()) {
            //get genotype
            int geno = calledVar.getGenotype(sample.getIndex());
            //set genotypicInfo
            if (geno != Data.NA) {
                if (model.equals("dominant")) {
                    // Index Qualified
                    sampleIndexList.add(sample.getIndex());
                    if (isMinorRef) {
                        if (geno == Index.REF || geno == Index.HET || geno == Index.REF_MALE) {
                            genotypeList.add(1d);
                        } else if (geno == Index.HOM || geno == Index.HOM_MALE) {
                            genotypeList.add(0d);
                        }
                    } else if (geno == Index.REF || geno == Index.REF_MALE) {
                        genotypeList.add(0d);
                    } else if (geno == Index.HET || geno == Index.HOM || geno == Index.HOM_MALE) {
                        genotypeList.add(1d);
                    }
                }
            }
        }
    }

    private double getPValue(List<Double> genotypeInfo, List<Integer> sampleIndexList) {
        if (genotypeInfo.size() <= 1) {
            return Data.NA;
        }

        try {
            //Put indices
            MathManager.getRenjinEngine().put("ind", sampleIndexList);
            MathManager.getRenjinEngine().eval(" ind <- as.numeric(unlist(ind))");

            //Getting genotype info
            MathManager.getRenjinEngine().put("xt1", genotypeInfo);
            MathManager.getRenjinEngine().eval(" xt1 <- as.numeric(unlist(xt1))");

            //Getting covariate subset
            for (int i = 1; i <= SampleManager.getCovariateNum(); i++) {
                MathManager.getRenjinEngine().eval("xt" + (i + 1) + "<- x" + i + "[ind+1]");
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

    public static void initExpression() {
        int covariantCount = SampleManager.getCovariateNum() + 1;

        expression.append("yt~");

        for (int i = 0; i < covariantCount; i++) {
            expression.append("xt").append(i + 1);
            if (i != covariantCount - 1) {
                expression.append("+");
            }
        }
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
