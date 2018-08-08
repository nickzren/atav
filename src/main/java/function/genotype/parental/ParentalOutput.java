package function.genotype.parental;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Index;
import function.genotype.base.Carrier;
import global.Data;
import java.util.StringJoiner;
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
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family Id");
        sj.add("Sample Name (parent)");
        sj.add("Genotype (parent)");
        sj.add("Binomial (parent)");
        sj.add(getVariantDataTitle());
        sj.add(getAnnotationDataTitle());
        sj.add(getCarrierDataTitle());
        sj.add(getGenoStatDataTitle());
        sj.add(getExternalDataTitle());

        return sj.toString();
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
        StringJoiner sj = new StringJoiner(",");

        sj.add(child.getFamilyId());
        sj.add(parent.getName());
        sj.add(getGenoStr(parentGeno));
        sj.add(FormatManager.getDouble(parentBinomial));

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, calledVar.getCarrier(child.getId()), child);
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
