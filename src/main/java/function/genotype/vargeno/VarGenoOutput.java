package function.genotype.vargeno;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class VarGenoOutput extends Output {

    public static String getTitle() {
        return getVariantDataTitle()
                + getAnnotationDataTitle()
                + getCarrierDataTitle()
                + getGenoStatDataTitle()
                + getExternalDataTitle();
    }

    public VarGenoOutput(CalledVariant c) {
        super(c);
    }

    public String getString(Sample sample) {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, calledVar.getCarrier(sample.getId()), sample);
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
