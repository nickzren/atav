package function.cohort.parent;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.cohort.base.Sample;
import function.variant.base.Output;
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

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.add("Parent");
        sj.add("Comp Het Flag");
        sj.merge(initVarHeaderStr("1"));
        sj.merge(initVarHeaderStr("2"));

        return sj.toString();
    }

    private static StringJoiner initVarHeaderStr(String var) {
        String[] columnList = getHeaderByVariant().split(",");
        StringJoiner sj = new StringJoiner(",");

        for (String column : columnList) {
            sj.add(column + " (#" + var + ")");
        }

        return sj;
    }

    private static String getHeaderByVariant() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(Output.getVariantDataHeader());
        sj.merge(Output.getAnnotationDataHeader());

        sj.merge(initCarrierHeader("child"));
        sj.merge(initCarrierHeader("mother"));
        sj.merge(initCarrierHeader("father"));

        sj.merge(Output.getCohortLevelHeader());
        sj.merge(Output.getExternalDataHeader());

        return sj.toString();
    }

    private static StringJoiner initCarrierHeader(String str) {
        String[] columnList = Output.getCarrierDataHeader().toString().split(",");
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

    public byte getChildGT() {
        return calledVar.getGT(child.getIndex());
    }

    public byte getMotherGT() {
        return calledVar.getGT(mother.getIndex());
    }

    public byte getFatherGT() {
        return calledVar.getGT(father.getIndex());
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}