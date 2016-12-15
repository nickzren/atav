package function.genotype.collapsing;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Index;
import function.genotype.base.Carrier;
import global.Data;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class CompHetOutput extends CollapsingOutput implements Comparable {

    public static String getTitle() {
        return "Family ID,"
                + "Sample Name,"
                + "Sample Type,"
                + "Collapsed Gene,"
                + "Var Case Freq #1 & #2 (co-occurance),"
                + "Var Ctrl Freq #1 & #2 (co-occurance),"
                + initVarTitleStr("1") + ","
                + initVarTitleStr("2");
    }

    private static String initVarTitleStr(String var) {
        String varTitle = getVariantDataTitle()
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
                + "FS,"
                + "MQ,"
                + "QD,"
                + "Qual,"
                + "Read Pos Rank Sum,"
                + "MQ Rank Sum,"
                + "FILTER,"
                + "LOO MAF,";

        String[] list = varTitle.split(",");

        varTitle = "";

        boolean isFirst = true;

        for (String s : list) {
            if (isFirst) {
                varTitle += s + " (#" + var + ")";
                isFirst = false;
            } else {
                varTitle += "," + s + " (#" + var + ")";
            }
        }

        return varTitle;
    }

    public CompHetOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenotypeData(sb);

        Carrier carrier = calledVar.getCarrier(sample.getId());
        short adAlt = carrier != null ? carrier.getAdAlt() : Data.SHORT_NA;
        short adRef = carrier != null ? carrier.getADRef() : Data.SHORT_NA;
        sb.append(sample.getName()).append(",");
        sb.append(sample.getType()).append(",");
        sb.append(getGenoStr(calledVar.getGT(sample.getIndex()))).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getDP() : Data.SHORT_NA)).append(",");
        sb.append(FormatManager.getShort(calledVar.getDPBin(sample.getIndex()))).append(",");
        sb.append(FormatManager.getShort(adRef)).append(",");
        sb.append(FormatManager.getShort(adAlt)).append(",");
        sb.append(carrier != null ? carrier.getPercAltRead() : Data.STRING_NA).append(",");
        sb.append(FormatManager.getDouble(MathManager.getBinomial(adAlt + adRef, adAlt, 0.5))).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getGQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getFS() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getMQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getQD() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getQual() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getReadPosRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getMQRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(carrier != null ? carrier.getFILTER() : Data.STRING_NA).append(",");

        sb.append(FormatManager.getDouble(looMAF)).append(",");

        return sb.toString();
    }

    public boolean isHomOrRef(byte geno) {
        return geno == Index.HOM || geno == Index.REF;
    }

    @Override
    public int compareTo(Object another) throws ClassCastException {
        CollapsingOutput that = (CollapsingOutput) another;
        return this.geneName.compareTo(that.geneName); //small -> large
    }
}
