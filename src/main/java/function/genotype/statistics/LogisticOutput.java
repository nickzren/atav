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
import function.genotype.base.SampleManager;
import global.Data;
import global.Index;
import org.renjin.sexp.DoubleVector;
import utils.ErrorManager;
import utils.FormatManager;
import utils.MathManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            + "Major Hom Case,"
            + "Het Case,"
            + "Minor Hom Case,"
            + "Minor Hom Case Freq,"
            + "Het Case Freq,"
            + "Major Hom Ctrl,"
            + "Het Ctrl,"
            + "Minor Hom Ctrl,"
            + "Minor Hom Ctrl Freq,"
            + "Het Ctrl Freq,"
            + "Missing Case,"
            + "QC Fail Case,"
            + "Missing Ctrl,"
            + "QC Fail Ctrl,"
            + "Case Maf,"
            + "Ctrl Maf,"
            + "Case HWE_P,"
            + "Ctrl HWE_P,"
            + "P value,"
           // + "Avg Min Ctrl Cov,"
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
    private List<Double> pVals;
    private Map<String, List<Double>> modelGenoMap;
    private List<Integer> sampleIndexList;

    public LogisticOutput(CalledVariant c) {
        super(c);
    }

    public static String getTitle(){
        StringBuilder pValHeader = new StringBuilder();
        for (String s: StatisticsCommand.logisticModels)
            pValHeader.append(s+" P Value,");

        return LogisticOutput.title.replaceAll("P value,",pValHeader.toString().toUpperCase());
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

    public void doRegressionAll(){

        //pVals = new Double[StatisticsCommand.logisticModels.length];

        pVals= IntStream.range(0,StatisticsCommand.logisticModels.length)
                        .parallel()
                        .mapToObj(p ->  StatisticsCommand.logisticModels[p])
                        .mapToDouble(p -> doRegression(p))
                        .boxed()
                        .collect(Collectors.toList());



    }

    public double doRegression(String model) {
        double pValue;
        if (model.equals(RECESSIVE) && !isRecessive()) {
            pValue = Data.NA;
            return pValue;
        }

        List<Double> gt = modelGenoMap.get(model);

        if (null == gt || gt.size() <= 1) {
            pValue = Data.NA;
            return pValue;
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

            return pValue;
        } catch (Exception e) {
            ErrorManager.send(e);
            return Data.NA;
        }
    }

    public void initGenotypeAndSampleIndexList(String[] models) {
        this.modelGenoMap = new LinkedHashMap<>();
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
        sb.append(majorHomCount[Index.CASE]).append(",");
        sb.append(genoCount[Index.HET][Index.CASE]).append(",");
        sb.append(minorHomCount[Index.CASE]).append(",");
        sb.append(FormatManager.getDouble(minorHomFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hetFreq[Index.CASE])).append(",");
        sb.append(majorHomCount[Index.CTRL]).append(",");
        sb.append(genoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCount[Index.CTRL]).append(",");
        sb.append(FormatManager.getDouble(minorHomFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hetFreq[Index.CTRL])).append(",");
        sb.append(genoCount[Index.MISSING][Index.CASE]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CASE)).append(",");
        sb.append(genoCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(minorAlleleFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(minorAlleleFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
        //sb.append(FormatManager.getDouble(pValue)).append(",");
        //Appending all P values
        for (int i=0; i<StatisticsCommand.logisticModels.length; i++)
            sb.append(FormatManager.getDouble(pVals.get(i))).append(",");
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
        sb.append(calledVar.getMgi());

        return sb.toString();
    }
}
