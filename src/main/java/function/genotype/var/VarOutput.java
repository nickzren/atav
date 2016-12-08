package function.genotype.var;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;

/**
 *
 * @author nick
 */
public class VarOutput extends Output {

    public static String getTitle() {
        return getVariantDataTitle()
                + getAnnotationDataTitle()
                + getExternalDataTitle()
                + getGenotypeDataTitle();
    }

    public VarOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenotypeData(sb);

        return sb.toString();
    }
}
