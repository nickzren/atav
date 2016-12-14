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
                + "VQSLOD,"
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
        int readsAlt = carrier != null ? carrier.getAdAlt() : Data.INTEGER_NA;
        int readsRef = carrier != null ? carrier.getADRef() : Data.INTEGER_NA;
        sb.append(sample.getName()).append(",");
        sb.append(sample.getType()).append(",");
        sb.append(getGenoStr(calledVar.getGT(sample.getIndex()))).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getDP() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getInteger(calledVar.getDPBin(sample.getIndex()))).append(",");
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
