package function.genotype.vargeno;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import function.genotype.base.Carrier;
import global.Data;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class VarGenoOutput extends Output {

    public static String getTitle() {
        return getVariantDataTitle()
                + getAnnotationDataTitle()
                + getExternalDataTitle()
                + getGenotypeDataTitle()
                + "Sample Name,"
                + "Sample Type,"
                + "GT,"
                + "DP,"
                + "DP Bin,"
                + "AD REF,"
                + "AD ALT,"
                + "Percent Alt Read,"
                + "Percent Alt Read Binomial P,"
                + "GQ,"
                + "VQSLOD,"
                + "FS,"
                + "MQ,"
                + "QD,"
                + "Qual,"
                + "Read Pos Rank Sum,"
                + "MQ Rank Sum,"
                + "FILTER,";
    }

    public VarGenoOutput(CalledVariant c) {
        super(c);
    }

    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenotypeData(sb);

        Carrier carrier = calledVar.getCarrier(sample.getId());
        int readsAlt = carrier != null ? carrier.getAdAlt() : Data.INTEGER_NA;
        int readsRef = carrier != null ? carrier.getADRef() : Data.INTEGER_NA;
        sb.append(sample.getName()).append(",");
        sb.append(sample.getType()).append(",");
        sb.append(getGenoStr(calledVar.getGT(sample.getIndex()))).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getDP() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getShort(calledVar.getDPBin(sample.getIndex()))).append(",");
        sb.append(FormatManager.getInteger(readsRef)).append(",");
        sb.append(FormatManager.getInteger(readsAlt)).append(",");
        sb.append(FormatManager.getPercAltRead(readsAlt, carrier != null ? carrier.getDP() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getDouble(MathManager.getBinomial(readsAlt + readsRef, readsAlt, 0.5))).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getGQ() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getVqslod() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getFS() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getMQ() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getQD() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getQual() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getReadPosRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getMQRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(carrier != null ? carrier.getFILTER() : Data.STRING_NA).append(",");

        return sb.toString();
    }
}
