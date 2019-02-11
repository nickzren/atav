package function.genotype.parent;

import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
import function.genotype.base.Sample;
import function.variant.base.Output;
import global.Data;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class ParentOutput extends Output {

    // Family data
    Sample child;
    Sample mother;
    Sample father;
    Carrier cCarrier;
    Carrier mCarrier;
    Carrier fCarrier;

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.add("Parent");
        sj.add("Comp Het Flag");
        sj.merge(initVarTitleStr("1"));
        sj.merge(initVarTitleStr("2"));

        return sj.toString();
    }

    private static StringJoiner initVarTitleStr(String var) {
        String[] columnList = getTitleByVariant().split(",");
        StringJoiner sj = new StringJoiner(",");

        for (String column : columnList) {
            sj.add(column + " (#" + var + ")");
        }

        return sj;
    }

    private static String getTitleByVariant() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(Output.getVariantDataTitle());
        sj.merge(Output.getAnnotationDataTitle());

        sj.merge(initCarrierTitle("child"));
        sj.merge(initCarrierTitle("mother"));
        sj.merge(initCarrierTitle("father"));

        sj.merge(Output.getGenoStatDataTitle());
        sj.merge(Output.getExternalDataTitle());

        return sj.toString();
    }

    private static StringJoiner initCarrierTitle(String str) {
        String[] columnList = Output.getCarrierDataTitle().toString().split(",");
        StringJoiner sj = new StringJoiner(",");

        for (String column : columnList) {
            sj.add(column + " (" + str + ")");
        }

        return sj;
    }

    public ParentOutput(CalledVariant c) {
        super(c);
    }

    public void initFamilyData(Family family) {
        child = family.getChild();
        cCarrier = calledVar.getCarrier(child.getId());

        mother = family.getMother();
        mCarrier = calledVar.getCarrier(mother.getId());

        father = family.getFather();
        fCarrier = calledVar.getCarrier(father.getId());
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);

        getCarrierData(sj, cCarrier, child);
        getCarrierData(sj, mCarrier, mother);
        getCarrierData(sj, fCarrier, father);

        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj;
    }

    public byte getChildGeno() {
        return getGeno(cCarrier);
    }

    public byte getMotherGeno() {
        return getGeno(mCarrier);
    }

    public byte getFatherGeno() {
        return getGeno(fCarrier);
    }

    private byte getGeno(Carrier carrier) {
        if (carrier == null) {
            return Data.BYTE_NA;
        }

        return carrier.getGT();
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}