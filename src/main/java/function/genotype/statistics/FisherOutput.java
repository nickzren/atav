package function.genotype.statistics;

import function.genotype.base.CalledVariant;
import global.Data;
import global.Index;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.annotation.base.IntolerantScoreManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import utils.FormatManager;
import utils.LogManager;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class FisherOutput extends StatisticOutput {

    double oddsRatio = 0;
    public static final String title
            = "Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "C Score Phred,"
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
            + "Odds Ratio,"
            + "Avg Min Case Cov,"
            + "Avg Min Ctrl Cov,"
            + EvsManager.getTitle()
            + "Polyphen Humdiv Score,"
            + "Polyphen Humdiv Prediction,"
            + "Polyphen Humvar Score,"
            + "Polyphen Humvar Prediction,"
            + "Function,"
            + "Gene Name,"
            + IntolerantScoreManager.getTitle()
            + "Artifacts in Gene,"
            + "Codon Change,"
            + "Gene Transcript (AA Change),"
            + ExacManager.getTitle()
            + KaviarManager.getTitle()
            + KnownVarManager.getTitle();

    public FisherOutput(CalledVariant c) {
        super(c);
    }

    public boolean isValid(String model) {
        if (model.equals("recessive")) {
            if (!isRecessive()) {
                return false;
            }
        }

        if (isValid()
                && isCaseOnlyValid()
                && StatisticsCommand.isMinHomCaseRecValid(minorHomCase)) {
            return true;
        }

        return false;
    }

    boolean isCaseOnlyValid() {
        if (StatisticsCommand.isCaseOnly) {
            if (isMinorRef) {
                if ((sampleCount[Index.HET][Index.CASE]
                        + sampleCount[Index.REF][Index.CASE]
                        + sampleCount[Index.REF_MALE][Index.CASE]) > 0) {
                    return true;
                }
            } else {
                if ((sampleCount[Index.HET][Index.CASE]
                        + sampleCount[Index.HOM][Index.CASE]
                        + sampleCount[Index.HOM_MALE][Index.CASE]) > 0) {
                    return true;
                }
            }

            return false;
        }

        return true;
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
        sb.append(isMinorRef).append(",");
        sb.append(majorHomCase).append(",");
        sb.append(sampleCount[Index.HET][Index.CASE]).append(",");
        sb.append(minorHomCase).append(",");
        sb.append(FormatManager.getDouble(caseMhgf)).append(",");
        sb.append(FormatManager.getDouble(sampleFreq[Index.HET][Index.CASE])).append(",");
        sb.append(majorHomCtrl).append(",");
        sb.append(sampleCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCtrl).append(",");
        sb.append(FormatManager.getDouble(ctrlMhgf)).append(",");
        sb.append(FormatManager.getDouble(sampleFreq[Index.HET][Index.CTRL])).append(",");
        sb.append(sampleCount[Index.MISSING][Index.CASE]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CASE)).append(",");
        sb.append(sampleCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(caseMaf)).append(",");
        sb.append(FormatManager.getDouble(ctrlMaf)).append(",");
        sb.append(FormatManager.getDouble(caseHweP)).append(",");
        sb.append(FormatManager.getDouble(ctrlHweP)).append(",");
        sb.append(FormatManager.getDouble(pValue)).append(",");
        sb.append(FormatManager.getDouble(oddsRatio)).append(",");
        sb.append(FormatManager.getDouble(averageCov[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(averageCov[Index.CTRL])).append(",");

        sb.append(calledVar.getEvsStr());

        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(IntolerantScoreManager.getValues(calledVar.getGeneName())).append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(calledVar.getGeneName()))).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");

        sb.append(calledVar.getExacStr());

        sb.append(calledVar.getKaviarStr());

        sb.append(calledVar.getKnownVarStr());

        return sb.toString();
    }

    public void calculateP(ArrayList<Integer> countList) {
        if (countList.size() == 4) {
            pValue = FisherExact.getTwoTailedP(countList.get(0),
                    countList.get(1), countList.get(2),
                    countList.get(3));

            oddsRatio = getOddsRatio(countList.get(0),
                    countList.get(1), countList.get(2),
                    countList.get(3));
        } else if (countList.size() == 6) {
            pValue = FisherExact.getTwoTailedP(countList.get(0), countList.get(1),
                    countList.get(2), countList.get(3),
                    countList.get(4), countList.get(5));

            oddsRatio = Data.NA; // genotypic do not support oddsRatio
        }
    }

    private double getOddsRatio(double a, double b, double c, double d) {
        if (b * c == 0) {
            return Data.NA;
        }

        return (a / b) / (c / d);
    }

    /*
     * allelic model:(homAlleleNum + hetAlleleNum) compared to (refAlleleNum +
     * hetAlleleNum) Note: the count for male will be excluded on chrX
     *
     * dominant model: reference allele is major: (homSampleNum + hetSampleNum)
     * compared to refSampleNum reference allele is minor: (refSampleNum +
     * hetSampleNum) compared to homSampleNum
     *
     * recessive model: reference allele is major: homSampleNum compared to
     * (hetSampleNum + refSampleNum) reference allele is minor: refSampleNum
     * compared to (hetSampleNum + homSampleNum)
     *
     * genotypic model: homSampleNum compared to hetSampleNum compared to
     * refSampleNum
     */
    public void initCount(ArrayList<Integer> countList, String model) {
        if (model.equals("allelic")) {
            if (calledVar.getRegion().getChrNum() == 23) { // exclude male
                countList.add(2 * sampleCount[Index.HOM][Index.CASE]
                        + sampleCount[Index.HET][Index.CASE]);
                countList.add(2 * sampleCount[Index.REF][Index.CASE]
                        + sampleCount[Index.HET][Index.CASE]);
                countList.add(2 * sampleCount[Index.HOM][Index.CTRL]
                        + sampleCount[Index.HET][Index.CTRL]);
                countList.add(2 * sampleCount[Index.REF][Index.CTRL]
                        + sampleCount[Index.HET][Index.CTRL]);
            } else {
                countList.add(2 * sampleCount[Index.HOM][Index.CASE]
                        + sampleCount[Index.HOM_MALE][Index.CASE]
                        + sampleCount[Index.HET][Index.CASE]);
                countList.add(2 * sampleCount[Index.REF][Index.CASE]
                        + sampleCount[Index.REF_MALE][Index.CASE]
                        + sampleCount[Index.HET][Index.CASE]);
                countList.add(2 * sampleCount[Index.HOM][Index.CTRL]
                        + sampleCount[Index.HOM_MALE][Index.CTRL]
                        + sampleCount[Index.HET][Index.CTRL]);
                countList.add(2 * sampleCount[Index.REF][Index.CTRL]
                        + sampleCount[Index.REF_MALE][Index.CTRL]
                        + sampleCount[Index.HET][Index.CTRL]);
            }
        } else if (model.equals("dominant")) {
            if (isMinorRef) {
                countList.add(sampleCount[Index.REF][Index.CASE]
                        + sampleCount[Index.REF_MALE][Index.CASE]
                        + sampleCount[Index.HET][Index.CASE]);
                countList.add(sampleCount[Index.HOM][Index.CASE]
                        + sampleCount[Index.HOM_MALE][Index.CASE]);
                countList.add(sampleCount[Index.REF][Index.CTRL]
                        + sampleCount[Index.REF_MALE][Index.CTRL]
                        + sampleCount[Index.HET][Index.CTRL]);
                countList.add(sampleCount[Index.HOM][Index.CTRL]
                        + sampleCount[Index.HOM_MALE][Index.CTRL]);
            } else {
                countList.add(sampleCount[Index.HOM][Index.CASE]
                        + sampleCount[Index.HOM_MALE][Index.CASE]
                        + sampleCount[Index.HET][Index.CASE]);
                countList.add(sampleCount[Index.REF][Index.CASE]
                        + sampleCount[Index.REF_MALE][Index.CASE]);
                countList.add(sampleCount[Index.HOM][Index.CTRL]
                        + sampleCount[Index.HOM_MALE][Index.CTRL]
                        + sampleCount[Index.HET][Index.CTRL]);
                countList.add(sampleCount[Index.REF][Index.CTRL]
                        + sampleCount[Index.REF_MALE][Index.CTRL]);
            }
        } else if (model.equals("recessive")) {
            if (isMinorRef) {
                countList.add(sampleCount[Index.REF][Index.CASE]
                        + sampleCount[Index.REF_MALE][Index.CASE]);
                countList.add(sampleCount[Index.HOM][Index.CASE]
                        + sampleCount[Index.HOM_MALE][Index.CASE]
                        + sampleCount[Index.HET][Index.CASE]);
                countList.add(sampleCount[Index.REF][Index.CTRL]
                        + sampleCount[Index.REF_MALE][Index.CTRL]);
                countList.add(sampleCount[Index.HET][Index.CTRL]
                        + sampleCount[Index.HOM][Index.CTRL]
                        + sampleCount[Index.HOM_MALE][Index.CTRL]);
            } else {
                countList.add(sampleCount[Index.HOM][Index.CASE]
                        + sampleCount[Index.HOM_MALE][Index.CASE]);
                countList.add(sampleCount[Index.HET][Index.CASE]
                        + sampleCount[Index.REF][Index.CASE]
                        + sampleCount[Index.REF_MALE][Index.CASE]);
                countList.add(sampleCount[Index.HOM][Index.CTRL]
                        + sampleCount[Index.HOM_MALE][Index.CTRL]);
                countList.add(sampleCount[Index.HET][Index.CTRL]
                        + sampleCount[Index.REF][Index.CTRL]
                        + sampleCount[Index.REF_MALE][Index.CTRL]);
            }
        } else if (model.equals("genotypic")) {
            countList.add(sampleCount[Index.HOM][Index.CASE]
                    + sampleCount[Index.HOM_MALE][Index.CASE]);
            countList.add(sampleCount[Index.HET][Index.CASE]);
            countList.add(sampleCount[Index.REF][Index.CASE]
                    + sampleCount[Index.REF_MALE][Index.CASE]);
            countList.add(sampleCount[Index.HOM][Index.CTRL]
                    + sampleCount[Index.HOM_MALE][Index.CTRL]);
            countList.add(sampleCount[Index.HET][Index.CTRL]);
            countList.add(sampleCount[Index.REF][Index.CTRL]
                    + sampleCount[Index.REF_MALE][Index.CTRL]);
        } else {
            LogManager.writeAndPrint("not available model");
        }
    }
}
