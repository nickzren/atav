package function.cohort.trio;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.variant.base.Output;
import function.cohort.base.Sample;
import global.Data;
import global.Index;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class TrioOutput extends Output {

    String denovoFlag = "";

    // Trio Family data
    Sample child;
    Carrier cCarrier;
    byte cGeno;
    short cDPBin;
    String motherName;
    byte mGeno;
    short mDPBin;
    short mADAlt;
    short mDP;
    String fatherName;
    byte fGeno;
    short fDPBin;
    short fADAlt;
    short fDP;

    public TrioOutput(CalledVariant c) {
        super(c);
    }

    public void initTrioData(Trio trio) {
        child = trio.getChild();
        cGeno = calledVar.getGT(child.getIndex());
        cDPBin = calledVar.getDPBin(child.getIndex());
        cCarrier = calledVar.getCarrier(trio.getChild().getId());

        motherName = trio.getMotherName();
        mGeno = calledVar.getGT(trio.getMotherIndex());
        mDPBin = calledVar.getDPBin(trio.getMotherIndex());
        Carrier mCarrier = calledVar.getCarrier(trio.getMotherId());
        mADAlt = mCarrier == null ? Data.SHORT_NA : mCarrier.getADAlt();
        mDP = mCarrier == null ? Data.SHORT_NA : mCarrier.getDP();

        fatherName = trio.getFatherName();
        fGeno = calledVar.getGT(trio.getFatherIndex());
        fDPBin = calledVar.getDPBin(trio.getFatherIndex());
        Carrier fCarrier = calledVar.getCarrier(trio.getFatherId());
        fADAlt = fCarrier == null ? Data.SHORT_NA : fCarrier.getADAlt();
        fDP = fCarrier == null ? Data.SHORT_NA : fCarrier.getDP();
    }

    public void initDenovoFlag(Sample child) {
        byte mGenotype = convertMissing2HomRef(mGeno);
        byte fGenotype = convertMissing2HomRef(fGeno);

        denovoFlag = TrioManager.getStatus(calledVar.getChrNum(),
                child.isMale(),
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
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData_pgl(sj, cCarrier, child);
        sj.add(getGenoStr(mGeno));
        sj.add(FormatManager.getShort(mDPBin));
        sj.add(FormatManager.getShort(mADAlt));
        sj.add(FormatManager.getShort(mDP));
        sj.add(getGenoStr(fGeno));
        sj.add(FormatManager.getShort(fDPBin));
        sj.add(FormatManager.getShort(fADAlt));
        sj.add(FormatManager.getShort(fDP));
        sj.add(denovoFlag);
//        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
