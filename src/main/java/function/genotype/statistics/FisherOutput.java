package function.genotype.statistics;

import function.genotype.base.CalledVariant;
import global.Data;
import global.Index;
import function.external.evs.EvsManager;
import function.external.gnomad.GnomADManager;
import function.annotation.base.TranscriptManager;
import function.external.bis.BisManager;
import function.external.denovo.DenovoDBManager;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
import utils.FormatManager;
import utils.LogManager;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class FisherOutput extends StatisticOutput {

    double oddsRatio = 0;

    public static String getTitle() {
        return "Variant ID,"
                + "Variant Type,"
                + "Rs Number,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "CADD Score Phred,"
                + GerpManager.getTitle()
                + TrapManager.getTitle()
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
                + "P Value,"
                + "Odds Ratio,"
                + EvsManager.getTitle()
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Function,"
                + "Gene Name,"
                + "Transcript Stable Id,"
                + "Has CCDS Transcript,"
                + "Codon Change,"
                + "Gene Transcript (AA Change),"
                + ExacManager.getTitle()
                + GnomADManager.getExomeTitle()
                + GnomADManager.getGenomeTitle()
                + KaviarManager.getTitle()
                + KnownVarManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + BisManager.getTitle()
                + GenomesManager.getTitle()
                + MgiManager.getTitle()
                + DenovoDBManager.getTitle();
    }

    public FisherOutput(CalledVariant c) {
        super(c);
    }

    public boolean isValid(String model) {
        if (model.equals("recessive")) {
            if (!isRecessive()) {
                return false;
            }
        }

        return isValid()
                && isCaseOnlyValid();
    }

    boolean isCaseOnlyValid() {
        if (StatisticsCommand.isCaseOnly) {
            if (isMinorRef) {
                if ((genoCount[Index.HET][Index.CASE]
                        + genoCount[Index.REF][Index.CASE]
                        + genoCount[Index.REF_MALE][Index.CASE]) > 0) {
                    return true;
                }
            } else if ((genoCount[Index.HET][Index.CASE]
                    + genoCount[Index.HOM][Index.CASE]
                    + genoCount[Index.HOM_MALE][Index.CASE]) > 0) {
                return true;
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
        sb.append(calledVar.getGerpScore());
        sb.append(calledVar.getTrapScore());
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
        sb.append(FormatManager.getDouble(pValue)).append(",");
        sb.append(FormatManager.getDouble(oddsRatio)).append(",");
        sb.append(calledVar.getEvsStr());
        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");
        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(calledVar.getStableId()).append(",");
        sb.append(calledVar.hasCCDS()).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");
        sb.append(calledVar.getExacStr());
        sb.append(calledVar.getGnomADExomeStr());
        sb.append(calledVar.getGnomADGenomeStr());
        sb.append(calledVar.getKaviarStr());
        sb.append(calledVar.getKnownVarStr());
        sb.append(calledVar.getRvis());
        sb.append(calledVar.getSubRvis());
        sb.append(calledVar.getBis());
        sb.append(calledVar.get1000Genomes());
        sb.append(calledVar.getMgi());
        sb.append(calledVar.getDenovoDB());

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
        switch (model) {
            case "allelic":
                if (calledVar.getChrNum() == 23) { // exclude male
                    countList.add(2 * genoCount[Index.HOM][Index.CASE]
                            + genoCount[Index.HET][Index.CASE]);
                    countList.add(2 * genoCount[Index.REF][Index.CASE]
                            + genoCount[Index.HET][Index.CASE]);
                    countList.add(2 * genoCount[Index.HOM][Index.CTRL]
                            + genoCount[Index.HET][Index.CTRL]);
                    countList.add(2 * genoCount[Index.REF][Index.CTRL]
                            + genoCount[Index.HET][Index.CTRL]);
                } else {
                    countList.add(2 * genoCount[Index.HOM][Index.CASE]
                            + genoCount[Index.HOM_MALE][Index.CASE]
                            + genoCount[Index.HET][Index.CASE]);
                    countList.add(2 * genoCount[Index.REF][Index.CASE]
                            + genoCount[Index.REF_MALE][Index.CASE]
                            + genoCount[Index.HET][Index.CASE]);
                    countList.add(2 * genoCount[Index.HOM][Index.CTRL]
                            + genoCount[Index.HOM_MALE][Index.CTRL]
                            + genoCount[Index.HET][Index.CTRL]);
                    countList.add(2 * genoCount[Index.REF][Index.CTRL]
                            + genoCount[Index.REF_MALE][Index.CTRL]
                            + genoCount[Index.HET][Index.CTRL]);
                }
                break;
            case "dominant":
                if (isMinorRef) {
                    countList.add(genoCount[Index.REF][Index.CASE]
                            + genoCount[Index.REF_MALE][Index.CASE]
                            + genoCount[Index.HET][Index.CASE]);
                    countList.add(genoCount[Index.HOM][Index.CASE]
                            + genoCount[Index.HOM_MALE][Index.CASE]);
                    countList.add(genoCount[Index.REF][Index.CTRL]
                            + genoCount[Index.REF_MALE][Index.CTRL]
                            + genoCount[Index.HET][Index.CTRL]);
                    countList.add(genoCount[Index.HOM][Index.CTRL]
                            + genoCount[Index.HOM_MALE][Index.CTRL]);
                } else {
                    countList.add(genoCount[Index.HOM][Index.CASE]
                            + genoCount[Index.HOM_MALE][Index.CASE]
                            + genoCount[Index.HET][Index.CASE]);
                    countList.add(genoCount[Index.REF][Index.CASE]
                            + genoCount[Index.REF_MALE][Index.CASE]);
                    countList.add(genoCount[Index.HOM][Index.CTRL]
                            + genoCount[Index.HOM_MALE][Index.CTRL]
                            + genoCount[Index.HET][Index.CTRL]);
                    countList.add(genoCount[Index.REF][Index.CTRL]
                            + genoCount[Index.REF_MALE][Index.CTRL]);
                }
                break;
            case "recessive":
                if (isMinorRef) {
                    countList.add(genoCount[Index.REF][Index.CASE]
                            + genoCount[Index.REF_MALE][Index.CASE]);
                    countList.add(genoCount[Index.HOM][Index.CASE]
                            + genoCount[Index.HOM_MALE][Index.CASE]
                            + genoCount[Index.HET][Index.CASE]);
                    countList.add(genoCount[Index.REF][Index.CTRL]
                            + genoCount[Index.REF_MALE][Index.CTRL]);
                    countList.add(genoCount[Index.HET][Index.CTRL]
                            + genoCount[Index.HOM][Index.CTRL]
                            + genoCount[Index.HOM_MALE][Index.CTRL]);
                } else {
                    countList.add(genoCount[Index.HOM][Index.CASE]
                            + genoCount[Index.HOM_MALE][Index.CASE]);
                    countList.add(genoCount[Index.HET][Index.CASE]
                            + genoCount[Index.REF][Index.CASE]
                            + genoCount[Index.REF_MALE][Index.CASE]);
                    countList.add(genoCount[Index.HOM][Index.CTRL]
                            + genoCount[Index.HOM_MALE][Index.CTRL]);
                    countList.add(genoCount[Index.HET][Index.CTRL]
                            + genoCount[Index.REF][Index.CTRL]
                            + genoCount[Index.REF_MALE][Index.CTRL]);
                }
                break;
            case "genotypic":
                countList.add(genoCount[Index.HOM][Index.CASE]
                        + genoCount[Index.HOM_MALE][Index.CASE]);
                countList.add(genoCount[Index.HET][Index.CASE]);
                countList.add(genoCount[Index.REF][Index.CASE]
                        + genoCount[Index.REF_MALE][Index.CASE]);
                countList.add(genoCount[Index.HOM][Index.CTRL]
                        + genoCount[Index.HOM_MALE][Index.CTRL]);
                countList.add(genoCount[Index.HET][Index.CTRL]);
                countList.add(genoCount[Index.REF][Index.CTRL]
                        + genoCount[Index.REF_MALE][Index.CTRL]);
                break;
            default:
                LogManager.writeAndPrint("not available model");
                break;
        }
    }
}
