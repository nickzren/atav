package function.genotype.collapsing;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import java.util.HashSet;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class CollapsingOutput extends Output {

    public static String getTitle() {
        return getVariantDataTitle()
                + getAnnotationDataTitle()
                + getExternalDataTitle()
                + getGenoStatDataTitle()
                + getCarrierDataTitle()
                + "LOO MAF,";
    }

    double looMAF = 0;

    HashSet<String> regionBoundaryNameSet; // for --region-boundary only

    public CollapsingOutput(CalledVariant c) {
        super(c);
    }

    public void initRegionBoundaryNameSet() {
        regionBoundaryNameSet = RegionBoundaryManager.getNameSet(
                calledVar.getChrStr(),
                calledVar.getStartPosition());
    }

    public void calculateLooFreq(Sample sample) {
        if (sample.getId() != Data.INTEGER_NA) {
            byte geno = calledVar.getGT(sample.getIndex());

            calledVar.deleteSampleGeno(geno, sample);

            calculateLooMaf();

            calledVar.addSampleGeno(geno, sample);
        }
    }

    private void calculateLooMaf() {
        int alleleCount = 2 * calledVar.genoCount[Index.HOM][Index.CASE]
                + calledVar.genoCount[Index.HOM_MALE][Index.CASE]
                + calledVar.genoCount[Index.HET][Index.CASE]
                + 2 * calledVar.genoCount[Index.HOM][Index.CTRL]
                + calledVar.genoCount[Index.HOM_MALE][Index.CTRL]
                + calledVar.genoCount[Index.HET][Index.CTRL];
        int totalCount = alleleCount
                + calledVar.genoCount[Index.HET][Index.CASE]
                + 2 * calledVar.genoCount[Index.REF][Index.CASE]
                + calledVar.genoCount[Index.REF_MALE][Index.CASE]
                + calledVar.genoCount[Index.HET][Index.CTRL]
                + 2 * calledVar.genoCount[Index.REF][Index.CTRL]
                + calledVar.genoCount[Index.REF_MALE][Index.CTRL];

        double allAF = MathManager.devide(alleleCount, totalCount);
        looMAF = allAF;

        if (allAF > 0.5) {
            looMAF = 1.0 - allAF;
        }
    }

    public boolean isMaxLooMafValid() {
        return CollapsingCommand.isMaxLooMafValid(looMAF);
    }

    /*
     * if ref is minor then only het & ref are qualified samples. If ref is
     * major then only hom & het are qualified samples.
     */
    @Override
    public boolean isQualifiedGeno(byte geno) {
        if (CollapsingCommand.isRecessive && geno == Index.HET) {
            return false;
        }

        return super.isQualifiedGeno(geno);
    }

    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenoStatData(sb);
        getCarrierData(sb, calledVar.getCarrier(sample.getId()), sample);

        sb.append(FormatManager.getDouble(looMAF)).append(",");

        return sb.toString();
    }
}
