package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import function.genotype.base.SampleManager;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class TrioOutput extends Output implements Comparable {

    String denovoFlag = "";

    // Trio Family data
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

    public TrioOutput(CalledVariant c) {
        super(c);
    }

    public void initTrioFamilyData(Trio trio) {
        child = trio.getChild();
        cGeno = calledVar.getGT(child.getIndex());
        cDPBin = calledVar.getDPBin(child.getIndex());
        cCarrier = calledVar.getCarrier(trio.getChild().getId());

        motherName = trio.getMotherName();
        mGeno = calledVar.getGT(trio.getMotherIndex());
        mDPBin = calledVar.getDPBin(trio.getMotherIndex());

        fatherName = trio.getFatherName();
        fGeno = calledVar.getGT(trio.getFatherIndex());
        fDPBin = calledVar.getDPBin(trio.getFatherIndex());
    }

    public void deleteParentGeno(Trio trio) {
        deleteSampleGeno(trio.getMotherId());

        deleteSampleGeno(trio.getFatherId());
    }

    public void deleteSampleGeno(int id) {
        if (id != Data.INTEGER_NA) {
            Sample sample = SampleManager.getMap().get(id);

            byte geno = calledVar.getGT(sample.getIndex());
            geno = getGenoType(geno, sample);

            deleteSampleGeno(geno, sample.getPheno());

            genoCount[Index.MISSING][sample.getPheno()]++;
        }
    }

    public void addParentGeno(Trio trio) {
        addSampleGeno(trio.getMotherId());

        addSampleGeno(trio.getFatherId());
    }

    public void addSampleGeno(int id) {
        if (id != Data.INTEGER_NA) {
            Sample sample = SampleManager.getMap().get(id);

            byte geno = calledVar.getGT(sample.getIndex());
            byte type = getGenoType(geno, sample);

            addSampleGeno(type, sample.getPheno());

            genoCount[Index.MISSING][sample.getPheno()]--;
        }
    }

    public void initDenovoFlag(Sample child) {
        byte mGenotype = convertMissing2HomRef(mGeno);
        byte fGenotype = convertMissing2HomRef(fGeno);

        denovoFlag = TrioManager.getStatus(calledVar.getChrNum(),
                !isMinorRef, child.isMale(),
                cGeno, cDPBin,
                mGenotype, mDPBin,
                fGenotype, fDPBin);
    }

    /*
     * convert all missing genotype to hom ref for parents
     */
    private byte convertMissing2HomRef(byte geno) {
        if (geno == Data.BYTE_NA) {
            return Index.REF;
        }

        return geno;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenotypeData(sb);

        short cAdAlt = cCarrier != null ? cCarrier.getAdAlt() : Data.SHORT_NA;
        short cAdRef = cCarrier != null ? cCarrier.getADRef() : Data.SHORT_NA;
        sb.append(getGenoStr(mGeno)).append(",");
        sb.append(FormatManager.getShort(mDPBin)).append(",");
        sb.append(getGenoStr(fGeno)).append(",");
        sb.append(FormatManager.getShort(fDPBin)).append(",");
        sb.append(getGenoStr(cGeno)).append(",");
        sb.append(FormatManager.getShort(cCarrier != null ? cCarrier.getDP() : Data.SHORT_NA)).append(",");
        sb.append(FormatManager.getShort(cDPBin)).append(",");
        sb.append(FormatManager.getShort(cAdRef)).append(",");
        sb.append(FormatManager.getShort(cAdAlt)).append(",");
        sb.append(cCarrier != null ? cCarrier.getPercAltRead() : Data.STRING_NA).append(",");
        sb.append(FormatManager.getDouble(MathManager.getBinomial(cAdAlt + cAdRef, cAdAlt, 0.5))).append(",");
        sb.append(FormatManager.getByte(cCarrier != null ? cCarrier.getGQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getFloat(cCarrier != null ? cCarrier.getVqslod() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(cCarrier != null ? cCarrier.getFS() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getByte(cCarrier != null ? cCarrier.getMQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getByte(cCarrier != null ? cCarrier.getQD() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getInteger(cCarrier != null ? cCarrier.getQual() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getFloat(cCarrier != null ? cCarrier.getReadPosRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(cCarrier != null ? cCarrier.getMQRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(cCarrier != null ? cCarrier.getFILTER() : Data.STRING_NA).append(",");

        return sb.toString();
    }

    @Override
    public int compareTo(Object another) throws ClassCastException {
        TrioOutput that = (TrioOutput) another;
        return this.getCalledVariant().getGeneName().compareTo(
                that.getCalledVariant().getGeneName()); //small -> large
    }
}
