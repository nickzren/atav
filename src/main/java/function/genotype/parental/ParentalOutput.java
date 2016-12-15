package function.genotype.parental;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
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
public class ParentalOutput extends Output {

    Sample child;
    byte childGeno;
    double childBinomial;

    Sample parent;
    byte parentGeno;
    double parentBinomial;

    public static String getTitle() {
        return "Family Id,"
                + "Sample Name (child),"
                + "Genotype (child),"
                + "Binomial (child),"
                + "Sample Name (parent),"
                + "Genotype (parent),"
                + "Binomial (parent),"
                + getVariantDataTitle()
                + getAnnotationDataTitle()
                + getExternalDataTitle()
                + getGenotypeDataTitle()
                + "DP,"
                + "DP Bin,"
                + "AD REF,"
                + "AD ALT,"
                + "Percent Alt Read,"
                + "GQ,"
                + "FS,"
                + "MQ,"
                + "QD,"
                + "Qual,"
                + "Read Pos Rank Sum,"
                + "MQ Rank Sum,"
                + "FILTER,";
    }

    public ParentalOutput(CalledVariant c) {
        super(c);
    }

    public boolean isChildValid(Sample child) {
        this.child = child;
        Carrier carrier = calledVar.getCarrier(child.getId());

        return isChildGenoValid()
                && isChildQdValid(carrier)
                && isChildHetPercentAltReadValid(carrier)
                && isChildBinomialValid(carrier);
    }

    private boolean isChildGenoValid() {
        childGeno = calledVar.getGT(child.getIndex());

        return isQualifiedGeno(childGeno);
    }

    private boolean isChildQdValid(Carrier carrier) {
        byte value = Data.BYTE_NA;

        if (ParentalCommand.childQD != Data.NO_FILTER) {
            value = carrier != null ? carrier.getQD() : Data.BYTE_NA;
        }

        return ParentalCommand.isChildQdValid(value);
    }

    private boolean isChildHetPercentAltReadValid(Carrier carrier) {
        double percAltRead = Data.DOUBLE_NA;

        if (ParentalCommand.childHetPercentAltRead != null
                && childGeno == Index.HET) {
            int readsAlt = carrier != null ? carrier.getADAlt() : Data.INTEGER_NA;
            int gatkFilteredCoverage = carrier != null ? carrier.getDP() : Data.INTEGER_NA;

            percAltRead = MathManager.devide(readsAlt, gatkFilteredCoverage);
        }

        return ParentalCommand.isChildHetPercentAltReadValid(percAltRead);
    }

    private boolean isChildBinomialValid(Carrier carrier) {
        childBinomial = carrier != null ? carrier.getPercentAltReadBinomialP() : Data.DOUBLE_NA;

        return ParentalCommand.isChildBinomialValid(childBinomial);
    }

    public boolean isParentValid(Sample parent) {
        this.parent = parent;
        parentGeno = calledVar.getGT(parent.getIndex());

        return isParentBinomialValid();
    }

    private boolean isParentBinomialValid() {
        Carrier carrier = calledVar.getCarrier(parent.getId());

        parentBinomial = carrier != null ? carrier.getPercentAltReadBinomialP() : Data.DOUBLE_NA;

        return ParentalCommand.isParentBinomialValid(parentBinomial);
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append(child.getFamilyId()).append(",");
        sb.append(child.getName()).append(",");
        sb.append(getGenoStr(childGeno)).append(",");
        sb.append(FormatManager.getDouble(childBinomial)).append(",");
        sb.append(parent.getName()).append(",");
        sb.append(getGenoStr(parentGeno)).append(",");
        sb.append(FormatManager.getDouble(parentBinomial)).append(",");

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenotypeData(sb);

        Carrier carrier = calledVar.getCarrier(child.getId());
        sb.append(FormatManager.getShort(carrier != null ? carrier.getDP() : Data.SHORT_NA)).append(",");
        sb.append(FormatManager.getShort(calledVar.getDPBin(child.getIndex()))).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getADRef() : Data.SHORT_NA)).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getADAlt() : Data.SHORT_NA)).append(",");
        sb.append(carrier != null ? carrier.getPercAltRead() : Data.STRING_NA).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getGQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getFS() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getMQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getQD() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getQual() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getReadPosRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getMQRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(carrier != null ? carrier.getFILTER() : Data.STRING_NA).append(",");

        return sb.toString();
    }
}
