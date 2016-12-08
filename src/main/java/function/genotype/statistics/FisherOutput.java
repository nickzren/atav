package function.genotype.statistics;

import function.genotype.base.CalledVariant;
import global.Data;
import global.Index;
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
        return getVariantDataTitle()
                + getAnnotationDataTitle()
                + getExternalDataTitle()
                + getGenotypeDataTitle()
                + "P Value,"
                + "Odds Ratio,";
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

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenotypeData(sb);

        sb.append(FormatManager.getDouble(pValue)).append(",");
        sb.append(FormatManager.getDouble(oddsRatio)).append(",");

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
