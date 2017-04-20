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
                + getGenoStatDataTitle()
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

        return isCaseOnlyValid();
    }

    boolean isCaseOnlyValid() {
        if (StatisticsCommand.isCaseOnly) {
            if ((calledVar.genoCount[Index.HET][Index.CASE]
                    + calledVar.genoCount[Index.HOM][Index.CASE]
                    + calledVar.genoCount[Index.HOM_MALE][Index.CASE]) > 0) {
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
        getGenoStatData(sb);

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

            oddsRatio = Data.DOUBLE_NA; // genotypic do not support oddsRatio
        }
    }

    private double getOddsRatio(double a, double b, double c, double d) {
        if (b * c == 0) {
            return Data.DOUBLE_NA;
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
                countList.add(2 * calledVar.genoCount[Index.HOM][Index.CASE]
                        + calledVar.genoCount[Index.HOM_MALE][Index.CASE]
                        + calledVar.genoCount[Index.HET][Index.CASE]);
                countList.add(2 * calledVar.genoCount[Index.REF][Index.CASE]
                        + calledVar.genoCount[Index.REF_MALE][Index.CASE]
                        + calledVar.genoCount[Index.HET][Index.CASE]);
                countList.add(2 * calledVar.genoCount[Index.HOM][Index.CTRL]
                        + calledVar.genoCount[Index.HOM_MALE][Index.CTRL]
                        + calledVar.genoCount[Index.HET][Index.CTRL]);
                countList.add(2 * calledVar.genoCount[Index.REF][Index.CTRL]
                        + calledVar.genoCount[Index.REF_MALE][Index.CTRL]
                        + calledVar.genoCount[Index.HET][Index.CTRL]);
                break;
            case "dominant":
                countList.add(calledVar.genoCount[Index.HOM][Index.CASE]
                        + calledVar.genoCount[Index.HOM_MALE][Index.CASE]
                        + calledVar.genoCount[Index.HET][Index.CASE]);
                countList.add(calledVar.genoCount[Index.REF][Index.CASE]
                        + calledVar.genoCount[Index.REF_MALE][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HOM][Index.CTRL]
                        + calledVar.genoCount[Index.HOM_MALE][Index.CTRL]
                        + calledVar.genoCount[Index.HET][Index.CTRL]);
                countList.add(calledVar.genoCount[Index.REF][Index.CTRL]
                        + calledVar.genoCount[Index.REF_MALE][Index.CTRL]);
                break;
            case "recessive":
                countList.add(calledVar.genoCount[Index.HOM][Index.CASE]
                        + calledVar.genoCount[Index.HOM_MALE][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HET][Index.CASE]
                        + calledVar.genoCount[Index.REF][Index.CASE]
                        + calledVar.genoCount[Index.REF_MALE][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HOM][Index.CTRL]
                        + calledVar.genoCount[Index.HOM_MALE][Index.CTRL]);
                countList.add(calledVar.genoCount[Index.HET][Index.CTRL]
                        + calledVar.genoCount[Index.REF][Index.CTRL]
                        + calledVar.genoCount[Index.REF_MALE][Index.CTRL]);
                break;
            case "genotypic":
                countList.add(calledVar.genoCount[Index.HOM][Index.CASE]
                        + calledVar.genoCount[Index.HOM_MALE][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HET][Index.CASE]);
                countList.add(calledVar.genoCount[Index.REF][Index.CASE]
                        + calledVar.genoCount[Index.REF_MALE][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HOM][Index.CTRL]
                        + calledVar.genoCount[Index.HOM_MALE][Index.CTRL]);
                countList.add(calledVar.genoCount[Index.HET][Index.CTRL]);
                countList.add(calledVar.genoCount[Index.REF][Index.CTRL]
                        + calledVar.genoCount[Index.REF_MALE][Index.CTRL]);
                break;
            default:
                LogManager.writeAndPrint("not available model");
                break;
        }
    }
}
