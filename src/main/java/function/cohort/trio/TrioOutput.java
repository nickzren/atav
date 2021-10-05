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
    boolean isDUO;

    byte tierFlag4SingleVar;
    byte isLoFDominantAndHaploinsufficient;
    byte isMissenseDominantAndHaploinsufficient;
    byte isKnownPathogenicVariant;
    byte isHotZone;

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

        isDUO = trio.isDUO();
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
                && (calledVar.isCarrierHetPercAltReadValid(cCarrier)
                || calledVar.isCarrieHomPercAltReadValid(cCarrier))
                && calledVar.isCarrierGATKQCValid(cCarrier)
                && isTrioDPBinValid()
                && calledVar.isGenotypeAbsentAmongControl(cCarrier.getGT());
    }

    // variant not detected in parents
    private boolean isVariantNotDetectedInParents() {
        return mGeno != Index.HOM && mGeno != Index.HET
                && fGeno != Index.HOM && fGeno != Index.HET;
    }

    // all family members have DP Bin >= 10
    private boolean isTrioDPBinValid() {
        return cDPBin >= 10 && mDPBin >= 10 && fDPBin >= 10;
    }

    public boolean isHomozygousTier1() {
        return denovoFlag.contains("HOMOZYGOUS")
                && isHetInBothParents()
                && calledVar.isHomozygousTier1(cCarrier);
    }

    // both parents are het carriers of variant
    private boolean isHetInBothParents() {
        return mGeno == Index.HET && fGeno == Index.HET;
    }

    public boolean isHemizygousTier1() {
        return denovoFlag.contains("HEMIZYGOUS")
                && isMotherHetAndFatherNotHom()
                && calledVar.isCarrieHomPercAltReadValid(cCarrier)
                && calledVar.isNotObservedInHomAmongControl()
                && cCarrier.getMQ() >= 40;
    }

    public boolean isCompoundDeletionTier1() {
        return denovoFlag.equals("COMPOUND DELETION")
                && isMotherOrFatherHet()
                && calledVar.isCarrieHomPercAltReadValid(cCarrier)
                && calledVar.isNotObservedInHomAmongControl()
                && cCarrier.getMQ() >= 40;
    }

    // mother is a het carrier and father is not hemizygous
    private boolean isMotherHetAndFatherNotHom() {
        return mGeno == Index.HET && fGeno != Index.HOM;
    }

    // one of the parent is a het carrier
    private boolean isMotherOrFatherHet() {
        return mGeno == Index.HET || fGeno == Index.HET;
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

    public boolean isCompoundDeletionTier2() {
        return denovoFlag.contains("COMPOUND DELETION")
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

    public void initTierFlag4SingleVar() {
        tierFlag4SingleVar = Data.BYTE_NA;

        // denovo or hom
        if (!denovoFlag.equals("NO FLAG") && !denovoFlag.equals(Data.STRING_NA)) {
            if (isDenovoTier1()
                    || isHomozygousTier1()
                    || isHemizygousTier1()
                    || isCompoundDeletionTier1()) {
                tierFlag4SingleVar = 1;
            } else if (calledVar.isMetTier2InclusionCriteria(cCarrier)
                    && (isDenovoTier2()
                    || isHomozygousTier2()
                    || isHemizygousTier2())
                    || isCompoundDeletionTier2()) {
                tierFlag4SingleVar = 2;
            }
        } else {
            if (calledVar.isMetTier2InclusionCriteria(cCarrier)
                    && calledVar.isCaseVarTier2(cCarrier)) {
                tierFlag4SingleVar = 2;
            }
        }

        isLoFDominantAndHaploinsufficient = calledVar.isLoFDominantAndHaploinsufficient(cCarrier);
        isMissenseDominantAndHaploinsufficient = calledVar.isMissenseDominantAndHaploinsufficient(cCarrier);
        isKnownPathogenicVariant = calledVar.isKnownPathogenicVariant();
        isHotZone = calledVar.isHotZone();
    }

    public byte getTierFlag4SingleVar() {
        return tierFlag4SingleVar;
    }

    public boolean isFlag() {
        return isLoFDominantAndHaploinsufficient == 1
                || calledVar.getKnownVar().isKnownVariant();
    }

    public void countSingleVar() {
        if (tierFlag4SingleVar == 1) {
            Output.tier1SingleVarCount++;
        } else if (tierFlag4SingleVar == 2) {
            Output.tier2SingleVarCount++;
        }

        if (isLoFDominantAndHaploinsufficient == 1) {
            Output.lofDominantAndHaploinsufficientCount++;
        }

        if (isMissenseDominantAndHaploinsufficient == 1) {
            Output.missenseDominantAndHaploinsufficientCount++;
        }

        if (isKnownPathogenicVariant == 1) {
            Output.knownPathogenicVarCount++;
        }

        if (isHotZone == 1) {
            Output.hotZoneVarCount++;
        }
    }

    public String getSummary() {
        StringJoiner sj = new StringJoiner("\n");

        sj.add("'" + calledVar.getGeneName() + "'");
        sj.add(calledVar.getVariantIdStr());
        sj.add(isDUO ? "DUO" : "TRIO");
        sj.add(calledVar.getEffect());
        sj.add(calledVar.getStableId());
        sj.add(calledVar.getHGVS_c());
        sj.add(calledVar.getHGVS_p());
        sj.add("DP = " + cCarrier.getDP());
        sj.add("PercAltRead = " + cCarrier.getPercAltReadStr());
        sj.add("GQ = " + cCarrier.getGQ());

        return sj.toString();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(calledVar.isMetTier2InclusionCriteria(cCarrier) ? 1 : 0));
        sj.add(FormatManager.getByte(isLoFDominantAndHaploinsufficient));
        sj.add(FormatManager.getByte(isMissenseDominantAndHaploinsufficient));
        sj.add(FormatManager.getByte(isKnownPathogenicVariant));
        sj.add(FormatManager.getByte(isHotZone));
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
