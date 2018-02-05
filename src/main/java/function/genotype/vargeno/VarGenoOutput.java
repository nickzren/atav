package function.genotype.vargeno;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;

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
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        getCarrierData(sb, calledVar.getCarrier(sample.getId()), sample);
        getGenoStatData(sb);
        calledVar.getExternalData(sb);

        return sb.toString();
    }

    public String getJointedGenotypeString(String genoArrayStr) {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        sb.append("NA,NA,NA,");
        sb.append(genoArrayStr).append(",");
        sb.append("NA,");
        sb.append("NA,"); // DP Bin
        sb.append("NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,");
        getGenoStatData(sb);
        calledVar.getExternalData(sb);

        return sb.toString();
    }
}
