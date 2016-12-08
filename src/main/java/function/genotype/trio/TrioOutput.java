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
    int cGeno;
    int cDPBin;
    String motherName;
    int mGeno;
    int mDPBin;
    String fatherName;
    int fGeno;
    int fDPBin;

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
        if (id != Data.NA) {
            Sample sample = SampleManager.getMap().get(id);

            int geno = calledVar.getGT(sample.getIndex());
            int type = getGenoType(geno, sample);

            deleteSampleGeno(type, sample.getPheno());

            genoCount[Index.MISSING][sample.getPheno()]++;
        }
    }

    public void addParentGeno(Trio trio) {
        addSampleGeno(trio.getMotherId());

        addSampleGeno(trio.getFatherId());
    }

    public void addSampleGeno(int id) {
        if (id != Data.NA) {
            Sample sample = SampleManager.getMap().get(id);

            int geno = calledVar.getGT(sample.getIndex());
            int type = getGenoType(geno, sample);

            addSampleGeno(type, sample.getPheno());

            genoCount[Index.MISSING][sample.getPheno()]--;
        }
    }

    public void initDenovoFlag(Sample child) {
        int mGenotype = convertMissing2HomRef(mGeno);
        int fGenotype = convertMissing2HomRef(fGeno);

        denovoFlag = TrioManager.getStatus(calledVar.getChrNum(),
                !isMinorRef, child.isMale(),
                cGeno, cDPBin,
                mGenotype, mDPBin,
                fGenotype, fDPBin);
    }

    /*
     * convert all missing genotype to hom ref for parents
     */
    private int convertMissing2HomRef(int geno) {
        if (geno == Data.NA) {
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

        int cReadsAlt = cCarrier != null ? cCarrier.getAdAlt() : Data.NA;
        int cReadsRef = cCarrier != null ? cCarrier.getADRef() : Data.NA;
        sb.append(getGenoStr(mGeno)).append(",");
        sb.append(FormatManager.getDouble(mDPBin)).append(",");
        sb.append(getGenoStr(fGeno)).append(",");
        sb.append(FormatManager.getDouble(fDPBin)).append(",");
        sb.append(getGenoStr(cGeno)).append(",");
        sb.append(FormatManager.getInteger(cCarrier != null ? cCarrier.getDP() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(cDPBin)).append(",");
        sb.append(FormatManager.getInteger(cReadsRef)).append(",");
        sb.append(FormatManager.getInteger(cReadsAlt)).append(",");
        sb.append(FormatManager.getPercAltRead(cReadsAlt, cCarrier != null ? cCarrier.getDP() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(MathManager.getBinomial(cReadsAlt + cReadsRef, cReadsAlt, 0.5))).append(",");
        sb.append(FormatManager.getInteger(cCarrier != null ? cCarrier.getGQ() : Data.NA)).append(",");
        sb.append(FormatManager.getFloat(cCarrier != null ? cCarrier.getVqslod() : Data.NA)).append(",");
        sb.append(FormatManager.getFloat(cCarrier != null ? cCarrier.getFS() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(cCarrier != null ? cCarrier.getMQ() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(cCarrier != null ? cCarrier.getQD() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(cCarrier != null ? cCarrier.getQual() : Data.NA)).append(",");
        sb.append(FormatManager.getFloat(cCarrier != null ? cCarrier.getReadPosRankSum() : Data.NA)).append(",");
        sb.append(FormatManager.getFloat(cCarrier != null ? cCarrier.getMQRankSum() : Data.NA)).append(",");
        sb.append(cCarrier != null ? cCarrier.getFILTER() : "NA").append(",");

        return sb.toString();
    }

    @Override
    public int compareTo(Object another) throws ClassCastException {
        TrioOutput that = (TrioOutput) another;
        return this.getCalledVariant().getGeneName().compareTo(
                that.getCalledVariant().getGeneName()); //small -> large
    }
}
