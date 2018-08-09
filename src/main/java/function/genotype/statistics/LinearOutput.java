package function.genotype.statistics;

import function.genotype.base.SampleManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import java.util.StringJoiner;
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
        StringJoiner sj = new StringJoiner(",");
        
        sj.merge(getVariantDataTitle());
        sj.merge(getAnnotationDataTitle());
        sj.merge(getExternalDataTitle());
        sj.merge(getGenoStatDataTitle());
        sj.add("P Value");
        sj.add("Beta1");
        
        return sj.toString();
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

        return true;
    }

    public void doRegression(String model) {
        SimpleRegression sr = new SimpleRegression(true);
        for (Sample sample : SampleManager.getList()) {
            byte geno = calledVar.getGT(sample.getIndex());
            if (geno != Data.INTEGER_NA) {
                float y = sample.getQuantitativeTrait();
                switch (model) {
                    case "allelic":
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
                        break;
                    case "dominant":
                        if (geno == Index.REF) {
                            sr.addData(0, y);
                        } else if (geno == Index.HET) {
                            sr.addData(1, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(1, y);
                        } 
                        break;
                    case "recessive":
                        if (geno == Index.REF) {
                            sr.addData(0, y);
                        } else if (geno == Index.HET) {
                            sr.addData(0, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(1, y);
                        } 
                        break;
                    case "genotypic":
                        // not complete yet, to be finished a bit later
                        if (geno == Index.REF) {
                            sr.addData(0, y);
                        } else if (geno == Index.HET) {
                            sr.addData(1, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(2, y);
                        } 
                        break;
                    case "additive":
                        if (geno == Index.REF) {
                            sr.addData(0, y);
                        } else if (geno == Index.HET) {
                            sr.addData(1, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(2, y);
                        }
                        break;
                    default:
                        LogManager.writeAndPrint("model is not recognized");
                        break;
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
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        calledVar.getExternalData(sj);
        getGenoStatData(sj);

        sj.add(FormatManager.getDouble(pValue));
        sj.add(FormatManager.getDouble(beta1));

        return sj.toString();
    }
}