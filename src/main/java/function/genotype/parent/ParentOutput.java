package function.genotype.parent;

import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
import function.genotype.base.Sample;
import function.variant.base.Output;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ParentOutput extends Output {

    // Family data
    Sample child;
    Carrier cCarrier;
    byte cGeno;
    short cDPBin;
    String motherName;
    byte mGeno;
    short mDPBin;
    String fatherName;
    byte fGeno;
    short fDPBin;

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.add("Parent");
        sj.add("Comp Het Flag");
        sj.add(initVarTitleStr("1"));
        sj.add(initVarTitleStr("2"));

        return sj.toString();
    }

    private static String initVarTitleStr(String var) {
        String[] columnList = getTitleByVariant().split(",");
        StringJoiner sj = new StringJoiner(",");

        for (String column : columnList) {
            sj.add(column + " (#" + var + ")");
        }

        return sj.toString();
    }

    private static String getTitleByVariant() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(Output.getVariantDataTitle());
        sj.add(Output.getAnnotationDataTitle());
        sj.add("GT (child)");
        sj.add("DP Bin (child)");
        sj.add("GT (mother)");
        sj.add("DP Bin (mother)");
        sj.add("GT (father)");
        sj.add("DP Bin (father)");
        sj.add(Output.getGenoStatDataTitle());
        sj.add(Output.getExternalDataTitle());

        return sj.toString();
    }

    public ParentOutput(CalledVariant c) {
        super(c);
    }

    public void initFamilyData(Family family) {
        child = family.getChild();
        cGeno = calledVar.getGT(child.getIndex());
        cDPBin = calledVar.getDPBin(child.getIndex());
        cCarrier = calledVar.getCarrier(family.getChild().getId());

        motherName = family.getMotherName();
        mGeno = calledVar.getGT(family.getMotherIndex());
        mDPBin = calledVar.getDPBin(family.getMotherIndex());

        fatherName = family.getFatherName();
        fGeno = calledVar.getGT(family.getFatherIndex());
        fDPBin = calledVar.getDPBin(family.getFatherIndex());
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        sj.add(getGenoStr(cGeno));
        sj.add(FormatManager.getShort(cDPBin));
        sj.add(getGenoStr(mGeno));
        sj.add(FormatManager.getShort(mDPBin));
        sj.add(getGenoStr(fGeno));
        sj.add(FormatManager.getShort(fDPBin));
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
