package function.cohort.singleton;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.variant.base.Output;
import function.cohort.base.Sample;
import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class SingletonOutput extends Output {

    Sample child;
    Carrier cCarrier;
    byte cGeno;
    short cDPBin;

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Comp Het Flag");
        sj.add("Compound Var");
        sj.add("Tier Flag (Compound Var)");
        sj.add("Tier Flag (Single Var)");
        sj.add("Pass Tier 2 Inclusion Criteria");
        sj.add("LoF Dominant and Haploinsufficient Gene");
        sj.add("Missense Dominant and Haploinsufficient Gene");
        sj.add("Known Pathogenic Variant");
        sj.add("Hot Zone");
        sj.merge(Output.getVariantDataHeader());
        sj.merge(Output.getAnnotationDataHeader());
        sj.merge(Output.getCarrierDataHeader());
        sj.merge(Output.getCohortLevelHeader());
        sj.merge(Output.getExternalDataHeader());

        return sj.toString();
    }

    public SingletonOutput(CalledVariant c) {
        super(c);
    }

    public void initSingletonData(Singleton singleton) {
        child = singleton.getChild();
        cGeno = calledVar.getGT(child.getIndex());
        cDPBin = calledVar.getDPBin(child.getIndex());
        cCarrier = calledVar.getCarrier(singleton.getChild().getId());
    }

    public byte getTierFlag4SingleVar() {
        byte tierFlag = Data.BYTE_NA;

        // Restrict to High or Moderate impact or TraP >= 0.4 variants
        if (getCalledVariant().isImpactHighOrModerate()) {
            if (calledVar.isHeterozygousTier1(cCarrier) ||
                    calledVar.isHomozygousTier1(cCarrier)) {
                tierFlag = 1;
                Output.tier1SingleVarCount++;
            } else if (calledVar.isMetTier2InclusionCriteria()
                    && calledVar.isCaseVarTier2(cCarrier)) {
                tierFlag = 2;
                Output.tier2SingleVarCount++;
            }
        }

        return tierFlag;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(calledVar.isMetTier2InclusionCriteria() ? 1 : 0));
        sj.add(FormatManager.getByte(calledVar.isLoFDominantAndHaploinsufficient(cCarrier)));
        sj.add(FormatManager.getByte(calledVar.isMissenseDominantAndHaploinsufficient(cCarrier)));
        sj.add(FormatManager.getByte(calledVar.isKnownPathogenicVariant()));
        sj.add(FormatManager.getByte(calledVar.isHotZone()));
        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, cCarrier, child);
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
