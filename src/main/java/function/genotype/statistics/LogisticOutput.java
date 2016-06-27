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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final String DOMINANT = "dominant";
    public static final String RECESSIVE = "recessive";

    private Map<String, List<Double>> modelGenoMap;
    private List<Integer> sampleIndexList;

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
        if (model.equals(RECESSIVE) && !isRecessive()) {
            pValue = Data.NA;
            return;
        }

        List<Double> gt = modelGenoMap.get(model);

        if (null == gt || gt.size() <= 1) {
            pValue = Data.NA;
            return;
        }

        try {
            //Put indices
            MathManager.getRenjinEngine().put("ind", sampleIndexList);
            MathManager.getRenjinEngine().eval(" ind <- as.numeric(unlist(ind))");

            //Getting genotype info
            MathManager.getRenjinEngine().put("xt1", gt);
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

            pValue = (null != res) ? res.getElementAsDouble(0) : Data.NA;
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void initGenotypeAndSampleIndexList(String[] models) {
        this.modelGenoMap = new HashMap<>();
        this.sampleIndexList = new ArrayList<>();

        //Dominant Model
        if (Arrays.asList(models).contains(DOMINANT)) {
            this.modelGenoMap.put(DOMINANT, SampleManager.getList()
                    .parallelStream() // !! Switching to parallel !!
                    .filter(sample -> calledVar.getGenotype(sample.getIndex()) != Data.NA)
                    .map(sample -> calledVar.getGenotype(sample.getIndex()))
                    .map((geno) -> {
                        double t = Data.NA;
                        if (isMinorRef) {
                            if (geno == Index.REF || geno == Index.HET || geno == Index.REF_MALE) {
                                t = 1;
                            } else if (geno == Index.HOM || geno == Index.HOM_MALE) {
                                t = 0;
                            }
                        } else if (geno == Index.REF || geno == Index.REF_MALE) {
                            t = 0;
                        } else if (geno == Index.HET || geno == Index.HOM || geno == Index.HOM_MALE) {
                            t = 1;
                        }
                        return t;
                    })
                    .collect(Collectors.toList()));
        }
        /**
         * Everything happens here*
         */
        //Recessive Model
        if (Arrays.asList(models).contains(RECESSIVE)
                && isRecessive()) { // has to match variant recessive rule
            this.modelGenoMap.put(RECESSIVE, SampleManager.getList()
                    .parallelStream() // !! Switching to parallel !!
                    .filter(sample -> calledVar.getGenotype(sample.getIndex()) != Data.NA)
                    .map(sample -> calledVar.getGenotype(sample.getIndex()))
                    .map((geno) -> {
                        double t = Data.NA;
                        if (isMinorRef) {
                            if (geno == Index.REF || geno == Index.REF_MALE) {
                                t = 1;
                            } else if (geno == Index.HOM || geno == Index.HOM_MALE || geno == Index.HET) {
                                t = 0;
                            }
                        } else if (geno == Index.HOM || geno == Index.HOM_MALE) {
                            t = 1;
                        } else if (geno == Index.HET || geno == Index.REF_MALE || geno == Index.REF) {
                            t = 0;
                        }
                        return t;
                    })
                    .collect(Collectors.toList()));
        }
        /**
         * ... and here*
         */

        //getting qualified indices
        this.sampleIndexList.addAll(
                SampleManager.getList()
                .parallelStream() // !! Switching to parallel !!
                .filter(sample -> calledVar.getGenotype(sample.getIndex()) != Data.NA)
                .map(sample -> sample.getIndex())
                .collect(Collectors.toList())
        );

        //Putting **Additive** on the burner
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
