package function.genotype.collapsing;

import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
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
    }

    String geneName = "";
    double looMAF = 0;

    HashSet<String> regionBoundaryNameSet; // for --region-boundary only

    public CollapsingOutput(CalledVariant c) {
        super(c);

        geneName = c.getGeneName();
    }

    public void initRegionBoundaryNameSet() {
        regionBoundaryNameSet = RegionBoundaryManager.getNameSet(
                calledVar.getChrStr(),
                calledVar.getStartPosition());
    }

    public void calculateLooFreq(Sample sample) {
        if (sample.getId() != Data.INTEGER_NA) {
            int geno = calledVar.getGT(sample.getIndex());
            int pheno = (int) sample.getPheno();
            int type = getGenoType(geno, sample);

            deleteSampleGeno(type, pheno);

            calculateLooMaf();

            addSampleGeno(type, pheno);
        }
    }

    private void calculateLooMaf() {
        int alleleCount = 2 * genoCount[Index.HOM][Index.ALL]
                + genoCount[Index.HET][Index.ALL]
                + genoCount[Index.HOM_MALE][Index.ALL];
        int totalCount = alleleCount + genoCount[Index.HET][Index.ALL]
                + 2 * genoCount[Index.REF][Index.ALL]
                + genoCount[Index.REF_MALE][Index.ALL];

        double allAF = MathManager.devide(alleleCount, totalCount);
        looMAF = allAF;

        if (allAF > 0.5) {
            isMinorRef = true;

            looMAF = 1.0 - allAF;
        } else {
            isMinorRef = false;
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
    public boolean isQualifiedGeno(int geno) {
        if (CollapsingCommand.isRecessive && geno == 1) {
            return false;
        }

        return super.isQualifiedGeno(geno);
    }

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
}
