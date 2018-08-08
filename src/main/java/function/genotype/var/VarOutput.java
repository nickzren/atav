package function.genotype.var;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class VarOutput extends Output {

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(getVariantDataTitle());
        sj.add(getAnnotationDataTitle());
        sj.add(getExternalDataTitle());
        sj.add(getGenoStatDataTitle());

        return sj.toString();
    }

    public VarOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        calledVar.getExternalData(sj);
        getGenoStatData(sj);

        return sj.toString();
    }
}
