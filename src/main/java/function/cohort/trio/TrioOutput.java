package function.cohort.trio;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.cohort.base.Enum.INHERITED_FROM;
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
    String fatherName;
    byte fGeno;
    short fDPBin;

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

        fatherName = trio.getFatherName();
        fGeno = calledVar.getGT(trio.getFatherIndex());
        fDPBin = calledVar.getDPBin(trio.getFatherIndex());
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

    public boolean isDenovoTier1() {
        return denovoFlag.contains("DE NOVO")
                && isVariantNotDetectedInParents()
                && (isChildHetPercAltReadValid() || isChildHomPercAltReadValid())
                && isChildGATKQCValid()
                && isTrioDPBinValid()
                && calledVar.isVariantAbsentAmongControl();
    }

    // variant not detected in parents
    private boolean isVariantNotDetectedInParents() {
        return mGeno != Index.HOM && mGeno != Index.HET
                && fGeno != Index.HOM && fGeno != Index.HET;
    }

    // child het carrier and >= 10% percent alt read
    private boolean isChildHetPercAltReadValid() {
        if (cGeno == Index.HET) {
            float percAltRead = cCarrier != null ? cCarrier.getPercAltRead() : Data.FLOAT_NA;

            return percAltRead != Data.FLOAT_NA && percAltRead >= 0.1;
        }

        return false;
    }

    // child Qual >= 50, QD >= 2, MQ >= 40
    private boolean isChildGATKQCValid() {
        if (cCarrier != null) {
            return cCarrier.getQual() >= 50
                    && cCarrier.getQD() >= 2
                    && cCarrier.getMQ() >= 40;
        }

        return false;
    }

    // all family members have DP Bin >= 10
    private boolean isTrioDPBinValid() {
        return cDPBin >= 10 && mDPBin >= 10 && fDPBin >= 10;
    }

    public boolean isHomozygousTier1() {
        return denovoFlag.contains("HOMOZYGOUS")
                && isHetInBothParents()
                && isChildHomPercAltReadValid()
                && calledVar.isNotObservedInHomAmongControl()
                && calledVar.isControlAFValid()
                && cCarrier.getMQ() >= 40;
    }

    // both parents are het carriers of variant
    private boolean isHetInBothParents() {
        return mGeno == Index.HET && fGeno == Index.HET;
    }

    // child hom carrier and >= 80% percent alt read
    private boolean isChildHomPercAltReadValid() {
        if (cGeno == Index.HOM) {
            float percAltRead = cCarrier != null ? cCarrier.getPercAltRead() : Data.FLOAT_NA;

            return percAltRead != Data.FLOAT_NA && percAltRead >= 0.8;
        }

        return false;
    }

    public boolean isHemizygousTier1() {
        return denovoFlag.contains("HEMIZYGOUS")
                && isMotherHetAndFatherNotHom()
                && isChildHomPercAltReadValid()
                && calledVar.isNotObservedInHomAmongControl()
                && cCarrier.getMQ() >= 40;
    }

    // mother is a het carrier and father is not hemizygous
    private boolean isMotherHetAndFatherNotHom() {
        return mGeno == Index.HET && fGeno != Index.HOM;
    }

    public boolean isDenovoTier2() {
        return denovoFlag.contains("DE NOVO")
                && isVariantNotDetectedInParents()
                && calledVar.isTotalACFromControlsValid();
    }

    public boolean isHomozygousTier2() {
        return denovoFlag.contains("HOMOZYGOUS")
                && calledVar.isNHomFromControlsValid(10);
    }

    public boolean isHemizygousTier2() {
        return denovoFlag.contains("HEMIZYGOUS")
                && calledVar.isNHomFromControlsValid(10);
    }

    // parents not hom
    public boolean isParentsNotHom() {
        return mGeno != Index.HOM && fGeno != Index.HOM;
    }

    public INHERITED_FROM getInheritedFrom() {
        if ((mGeno == Index.HOM || mGeno == Index.HET)
                && (fGeno == Index.HOM || fGeno == Index.HET)) {
            return INHERITED_FROM.BOTH;
        } else if (mGeno == Index.HOM || mGeno == Index.HET) {
            return INHERITED_FROM.MOTHER;
        } else if (fGeno == Index.HOM || fGeno == Index.HET) {
            return INHERITED_FROM.FATHER;
        } else {
            return INHERITED_FROM.NA;
        }
    }
    
    public byte getTierFlag4SingleVar() {
        byte tierFlag4SingleVar = Data.BYTE_NA;

        // denovo or hom
        if (!denovoFlag.equals("NO FLAG") && !denovoFlag.equals(Data.STRING_NA)) {
            if (isDenovoTier1()
                    || isHomozygousTier1()
                    || isHemizygousTier1()) {
                tierFlag4SingleVar = 1;
            } else if (calledVar.isMetTier2InclusionCriteria()
                    && (isDenovoTier2()
                    || isHomozygousTier2()
                    || isHemizygousTier2())) {
                tierFlag4SingleVar = 2;
            }
        } else { // child variant
            tierFlag4SingleVar = calledVar.isMetTier2InclusionCriteria() ? 2 : Data.BYTE_NA;
        }

        return tierFlag4SingleVar;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getByte(calledVar.isDominantAndClinGenHaploinsufficient(cCarrier)));
        sj.add(FormatManager.getByte(calledVar.isPreviouslyPathogenicReported(cCarrier)));
        sj.add(denovoFlag);
        sj.add(getInheritedFrom().name());
        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, cCarrier, child);
        sj.add(getGenoStr(mGeno));
        sj.add(FormatManager.getShort(mDPBin));
        sj.add(getGenoStr(fGeno));
        sj.add(FormatManager.getShort(fDPBin));
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
