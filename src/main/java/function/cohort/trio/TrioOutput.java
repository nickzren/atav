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
    Sample father;
    Sample mother;
    
    Carrier cCarrier;
    Carrier fCarrier;
    Carrier mCarrier;
    
    byte cGeno;
//    short cDPBin;
//    String motherName;
    byte mGeno;
//    short mDPBin;
//    String fatherName;
    byte fGeno;
//    short fDPBin;
//    boolean isDUO;

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
        father = trio.father;
        mother = trio.mother;
        
        cGeno = calledVar.getGT(child.getIndex());
        fGeno = calledVar.getGT(father.getIndex());
        mGeno = calledVar.getGT(mother.getIndex());
        
//        cCarrier = calledVar.getCarrier(trio.getChild().getId());
//        mCarrier = calledVar.getCarrier(trio.getMotherId());
//        fCarrier = calledVar.getCarrier(trio.getFatherId());
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

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, cCarrier, child);
        getCarrierData(sj, fCarrier, father);
        getCarrierData(sj, mCarrier, mother);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
