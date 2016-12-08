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
    int childGeno;
    double childBinomial;

    Sample parent;
    int parentGeno;
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
                + "VQSLOD,"
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
        float value = Data.NA;

        if (ParentalCommand.childQD != Data.NO_FILTER) {
            value = carrier != null ? carrier.getQD() : Data.NA;
        }

        return ParentalCommand.isChildQdValid(value);
    }

    private boolean isChildHetPercentAltReadValid(Carrier carrier) {
        double percAltRead = Data.NA;

        if (ParentalCommand.childHetPercentAltRead != null
                && childGeno == Index.HET) {
            int readsAlt = carrier != null ? carrier.getAdAlt() : Data.NA;
            int gatkFilteredCoverage = carrier != null ? carrier.getDP() : Data.NA;

            percAltRead = MathManager.devide(readsAlt, gatkFilteredCoverage);
        }

        return ParentalCommand.isChildHetPercentAltReadValid(percAltRead);
    }

    private boolean isChildBinomialValid(Carrier carrier) {
        int readsAlt = carrier != null ? carrier.getAdAlt() : Data.NA;
        int readsRef = carrier != null ? carrier.getADRef() : Data.NA;

        if (readsAlt == Data.NA || readsRef == Data.NA) {
            childBinomial = Data.NA;
        } else {
            childBinomial = MathManager.getBinomial(readsAlt + readsRef, readsAlt, 0.5);
        }

        return ParentalCommand.isChildBinomialValid(childBinomial);
    }

    public boolean isParentValid(Sample parent) {
        this.parent = parent;
        parentGeno = calledVar.getGT(parent.getIndex());

        return isParentBinomialValid();
    }

    private boolean isParentBinomialValid() {
        parentBinomial = Data.NA;

        Carrier carrier = calledVar.getCarrier(parent.getId());

        int readsAlt = carrier != null ? carrier.getAdAlt() : Data.NA;
        int readsRef = carrier != null ? carrier.getADRef() : Data.NA;

        if (readsAlt == Data.NA || readsRef == Data.NA) {
            parentBinomial = Data.NA;
        } else {
            parentBinomial = MathManager.getBinomial(readsAlt + readsRef, readsAlt, 0.5);
        }

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
        int readsAlt = carrier != null ? carrier.getAdAlt() : Data.NA;
        int readsRef = carrier != null ? carrier.getADRef() : Data.NA;
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getDP() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(calledVar.getDPBin(child.getIndex()))).append(",");
        sb.append(FormatManager.getInteger(readsRef)).append(",");
        sb.append(FormatManager.getInteger(readsAlt)).append(",");
        sb.append(FormatManager.getPercAltRead(readsAlt, carrier != null ? carrier.getDP() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getGQ() : Data.NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getVqslod() : Data.NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getFS() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getMQ() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getQD() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getQual() : Data.NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getReadPosRankSum() : Data.NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getMQRankSum() : Data.NA)).append(",");
        sb.append(carrier != null ? carrier.getFILTER() : "NA").append(",");

        return sb.toString();
    }
}
