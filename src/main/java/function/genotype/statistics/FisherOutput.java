package function.genotype.statistics;

import function.genotype.base.CalledVariant;
import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;
import global.Index;
import utils.FormatManager;
import utils.LogManager;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class FisherOutput extends StatisticOutput {

    double oddsRatio = 0;

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");
        
        sj.merge(getVariantDataTitle());
        sj.merge(getAnnotationDataTitle());
        sj.merge(getExternalDataTitle());
        sj.merge(getGenoStatDataTitle());
        sj.add("P Value");
        sj.add("Odds Ratio");
        
        return sj.toString();
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
        if (GenotypeLevelFilterCommand.isCaseOnly) {
            if ((calledVar.genoCount[Index.HET][Index.CASE]
                    + calledVar.genoCount[Index.HOM][Index.CASE]) > 0) {
                return true;
            }

            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        calledVar.getExternalData(sj);
        getGenoStatData(sj);

        sj.add(FormatManager.getDouble(pValue));
        sj.add(FormatManager.getDouble(oddsRatio));

        return sj.toString();
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
                        + calledVar.genoCount[Index.HET][Index.CASE]);
                countList.add(2 * calledVar.genoCount[Index.REF][Index.CASE]
                        + calledVar.genoCount[Index.HET][Index.CASE]);
                countList.add(2 * calledVar.genoCount[Index.HOM][Index.CTRL]
                        + calledVar.genoCount[Index.HET][Index.CTRL]);
                countList.add(2 * calledVar.genoCount[Index.REF][Index.CTRL]
                        + calledVar.genoCount[Index.HET][Index.CTRL]);
                break;
            case "dominant":
                countList.add(calledVar.genoCount[Index.HOM][Index.CASE]
                        + calledVar.genoCount[Index.HET][Index.CASE]);
                countList.add(calledVar.genoCount[Index.REF][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HOM][Index.CTRL]
                        + calledVar.genoCount[Index.HET][Index.CTRL]);
                countList.add(calledVar.genoCount[Index.REF][Index.CTRL]);
                break;
            case "recessive":
                countList.add(calledVar.genoCount[Index.HOM][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HET][Index.CASE]
                        + calledVar.genoCount[Index.REF][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HOM][Index.CTRL]);
                countList.add(calledVar.genoCount[Index.HET][Index.CTRL]
                        + calledVar.genoCount[Index.REF][Index.CTRL]);
                break;
            case "genotypic":
                countList.add(calledVar.genoCount[Index.HOM][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HET][Index.CASE]);
                countList.add(calledVar.genoCount[Index.REF][Index.CASE]);
                countList.add(calledVar.genoCount[Index.HOM][Index.CTRL]);
                countList.add(calledVar.genoCount[Index.HET][Index.CTRL]);
                countList.add(calledVar.genoCount[Index.REF][Index.CTRL]);
                break;
            default:
                LogManager.writeAndPrint("not available model");
                break;
        }
    }
}
