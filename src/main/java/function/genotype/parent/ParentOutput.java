package function.genotype.parent;

import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
import function.genotype.base.Sample;
import function.variant.base.Output;
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
        return "Family ID,"
                + "Parent,"
                + "Comp Het Flag,"
                + initVarTitleStr("1")
                + initVarTitleStr("2");
    }

    private static String initVarTitleStr(String var) {
        String[] columnList = getTitleByVariant().split(",");

        StringBuilder sb = new StringBuilder();

        for (String column : columnList) {
            sb.append(column).append(" (#").append(var).append(")" + ",");
        }

        return sb.toString();
    }

    private static String getTitleByVariant() {
        return Output.getVariantDataTitle()
                + Output.getAnnotationDataTitle()
                + "GT (child),"
                + "DP Bin (child),"
                + "GT (mother),"
                + "DP Bin (mother),"
                + "GT (father),"
                + "DP Bin (father),"
                + Output.getGenoStatDataTitle()
                + Output.getExternalDataTitle();
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
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        sb.append(getGenoStr(cGeno)).append(",");
        sb.append(FormatManager.getShort(cDPBin)).append(",");
        sb.append(getGenoStr(mGeno)).append(",");
        sb.append(FormatManager.getShort(mDPBin)).append(",");
        sb.append(getGenoStr(fGeno)).append(",");
        sb.append(FormatManager.getShort(fDPBin)).append(",");
        getGenoStatData(sb);
        calledVar.getExternalData(sb);

        return sb.toString();
    }
}
