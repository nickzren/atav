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
                + "LOO AF,";
    }

    double looAF = 0;

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

            calculateLooAF();

            calledVar.addSampleGeno(geno, sample);
        }
    }

    private void calculateLooAF() {
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

        looAF = MathManager.devide(alleleCount, totalCount);
    }

    public boolean isMaxLooAFValid() {
        return CollapsingCommand.isMaxLooAFValid(looAF);
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

        sb.append(FormatManager.getDouble(looAF)).append(",");
        
        return sb.toString();
    }
    
    public String getJointedGenotypeString(String genoArrayStr) {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenoStatData(sb);

        sb.append("NA,NA,NA,");
        sb.append(genoArrayStr).append(",");
        sb.append("NA,");
        sb.append("NA,"); // DP Bin
        sb.append("NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,");
        
        sb.append(FormatManager.getDouble(looAF)).append(",");

        return sb.toString();
    }
}
