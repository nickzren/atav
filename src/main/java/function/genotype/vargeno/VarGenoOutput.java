package function.genotype.vargeno;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class VarGenoOutput extends Output {

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");
        
        sj.merge(getVariantDataTitle());
        sj.merge(getAnnotationDataTitle());
        sj.merge(getCarrierDataTitle());
        sj.merge(getCohortLevelTitle());
        sj.add("LOO AF");
        sj.merge(getExternalDataTitle());
        
        return sj.toString();
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
        sj.add(FormatManager.getDouble(getLooAf()));
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
