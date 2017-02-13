package function.genotype.statistics;

import function.genotype.base.SampleManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import utils.FormatManager;
import utils.LogManager;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author nick
 */
public class LinearOutput extends StatisticOutput {

    double beta1 = 0;

    public static String getTitle() {
        return getVariantDataTitle()
                + getAnnotationDataTitle()
                + getExternalDataTitle()
                + getGenoStatDataTitle()
                + "P Value,"
                + "Beta1,";
    }

    public LinearOutput(CalledVariant c) {
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
        SimpleRegression sr = new SimpleRegression(true);
        for (Sample sample : SampleManager.getList()) {
            byte geno = calledVar.getGT(sample.getIndex());
            if (geno != Data.INTEGER_NA) {
                float y = sample.getQuantitativeTrait();
                if (model.equals("allelic")) {
                    if (geno == Index.REF) {
                        sr.addData(0, y);
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                        sr.addData(0, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(1, y);
                        sr.addData(1, y);
                    }
                } else if (model.equals("dominant")) {
                    if (geno == Index.REF) {
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(1, y);
                    }
                } else if (model.equals("recessive")) {
                    if (geno == Index.REF) {
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(0, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(1, y);
                    }
                } else if (model.equals("genotypic")) { // not complete yet, to be finished a bit later
                    if (geno == Index.REF) {
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(2, y);
                    }
                } else if (model.equals("additive")) {
                    if (geno == Index.REF) {
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(2, y);
                    }
                } else {
                    LogManager.writeAndPrint("model is not recognized");
                }
            }
        }
        pValue = sr.getSignificance();
        if (Double.isNaN(pValue)) {
            pValue = Data.DOUBLE_NA;
        }
        beta1 = sr.getSlope();
        if (Double.isNaN(beta1)) {
            beta1 = Data.DOUBLE_NA;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenoStatData(sb);

        sb.append(FormatManager.getDouble(pValue)).append(",");
        sb.append(FormatManager.getDouble(beta1)).append(",");

        return sb.toString();
    }
}
