package function.genotype.statistics;

import function.annotation.base.GeneManager;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import global.Data;
import global.Index;
import org.renjin.sexp.DoubleArrayVector;
import utils.ErrorManager;
import utils.FormatManager;
import utils.MathManager;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author nick, kaustubh
 */
public class LogisticOutput extends StatisticOutput {

    public static String getTitle() {
        return "Variant ID,"
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
                + "Dominant P value,"
                + "Recessive P value,"
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
                + GenomesManager.getTitle()
                + MgiManager.getTitle();
    }

    private static final StringBuilder expression = new StringBuilder();
    //
    List<Sample> qualifiedSamples;
    int[] qualifiedGeno;
    //Shifting everything to primitives
    private double [] pVals;
    private Map<String, int []> modelGenoMap;
    private int [] sampleIndexList;

    public LogisticOutput(CalledVariant c) {
        super(c);
    }

    public void doRegressionAll() {

        pVals = IntStream
                .range(0, StatisticsCommand.logisticModels.length)
                .parallel()
                .mapToObj(index -> StatisticsCommand.logisticModels[index])
                .mapToDouble(model -> doRegression(model))
                .toArray();
    }
    void setQualifiedGenoAndSamples(){
        qualifiedSamples=SampleManager.getList()
                .stream()
                //   .parallel() // !! Switching to parallel !!
                .filter(sample -> calledVar.getGenotype(sample.getIndex()) != Data.NA)
                .collect(Collectors.toList());

        qualifiedGeno=qualifiedSamples
                      .stream()
                      .mapToInt(sample -> calledVar.getGenotype(sample.getIndex()))
                      .toArray();
    }

    public double doRegression(String model) {
        if (model.equals("recessive") && !isRecessive()) {
            return Data.NA;
        }

        int [] genoList = modelGenoMap.get(model);

        if (null == genoList || genoList.length <= 1) {
            return Data.NA;
        }

        try {
            //Put indices
            MathManager.getRenjinEngine().put("ind", sampleIndexList);
            //Getting genotype info
            MathManager.getRenjinEngine().put("xt1", genoList);
            //Getting covariate subset
            for (int i = 1; i <= SampleManager.getCovariateNum(); i++) {
                MathManager.getRenjinEngine().eval("xt" + (i + 1) + "<- x" + i + "[ind+1]");
            }
            //Getting response subset
            MathManager.getRenjinEngine().eval("yt <- y[ind+1]");
            //Evaluating regression
            MathManager.getRenjinEngine().eval("logredmd <-glm(" + expression.toString() + ", family=\"binomial\" )");
            //Getting P value for genotype
            DoubleArrayVector res = (DoubleArrayVector) MathManager.getRenjinEngine().eval("coef(summary(logredmd))[2,4]");
            return (null != res) ? res.getElementAsDouble(0) : Data.NA;
        } catch (Exception e) {
            ErrorManager.send(e);
            return Data.NA;
        }
    }

    public void initGenoMapAndSampleIndexList() {
        this.modelGenoMap = new LinkedHashMap<>();
        setQualifiedGenoAndSamples();
        //Dominant Model
        this.modelGenoMap.put("dominant",Arrays
                .stream(qualifiedGeno)
                .map((geno) -> {
                    int t = Data.NA;
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
                .toArray()
        );

        //Additive model
        this.modelGenoMap.put("additive",Arrays
                .stream(qualifiedGeno)
                .map((geno) -> {
                    int t = Data.NA;
                    if (isMinorRef) {
                        if (geno == Index.HET || geno == Index.REF_MALE) {
                            t = 1;
                        } else if (geno == Index.HOM || geno == Index.HOM_MALE) {
                            t = 0;
                        } else if(geno == Index.REF){
                            t = 2;
                        }
                    } else if (geno == Index.REF || geno == Index.REF_MALE) {
                        t = 0;
                    } else if (geno == Index.HET || geno == Index.HOM_MALE) {
                        t = 1;
                    } else if (geno == Index.HOM ){
                        t = 2;
                    }
                    return t;
                })
                .toArray()
        );

        //Recessive Model
        if (isRecessive()) { // has to match variant recessive rule
            this.modelGenoMap.put("recessive", Arrays
                    .stream(qualifiedGeno)
                    .map((geno) -> {
                        int t = Data.NA;
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
                    .toArray()
            );
        }
        /**
         * ... and here*
         */


        //getting qualified indices
        sampleIndexList= qualifiedSamples
                        .stream()
                        .mapToInt(sample -> sample.getIndex())
                        .toArray();

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
        for (int i = 0; i < StatisticsCommand.logisticModels.length; i++) {
            sb.append(FormatManager.getDouble(pVals[i])).append(",");
        }
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
