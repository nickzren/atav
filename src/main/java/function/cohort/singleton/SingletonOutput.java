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

    byte tierFlag4SingleVar;
    byte isLoFDominantAndHaploinsufficient;
    byte isMissenseDominantAndHaploinsufficient;
    byte isKnownPathogenicVariant;
    byte isHotZone;

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.add("Proband");
        sj.add("Ancestry");
        sj.add("Broad Phenotype");
        sj.add("Gene Name");
        sj.add("Gene Link");
        sj.add("Compound Var");
        sj.add("Var Ctrl Freq #1 & #2 (co-occurance)");
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
        sj.add("Summary");

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

    public void initTierFlag4SingleVar() {
        tierFlag4SingleVar = Data.BYTE_NA;

        // Restrict to High or Moderate impact or TraP >= 0.4 variants
        if (getCalledVariant().isImpactHighOrModerate()) {
            if (calledVar.isHeterozygousTier1(cCarrier)
                    || calledVar.isHomozygousTier1(cCarrier)) {
                tierFlag4SingleVar = 1;
            } else if (calledVar.isMetTier2InclusionCriteria(cCarrier)
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
                || calledVar.getKnownVar().isKnownVariantSite();
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
        sj.add("SINGLETON");
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
        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, cCarrier, child);
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
