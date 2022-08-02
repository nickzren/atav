package function.cohort.singleton;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.variant.base.Output;
import function.cohort.base.Sample;
import global.Data;
import global.Index;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

    private LinkedHashSet<String> singleVariantPrioritizationSet = new LinkedHashSet<>();
    
    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Proband");
        sj.add("Single Variant Prioritization");
        sj.add("Compound Het Variant Prioritization");
        sj.add("Gene Name");
        sj.add("Gene Link");
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
        sj.merge(Output.getCarrierDataHeader_pgl());
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
        if (!singleVariantPrioritizationSet.isEmpty()) {
            return;
        }
        
        isLoFDominantAndHaploinsufficient = calledVar.isLoFDominantAndHaploinsufficient(cCarrier);
        isMissenseDominantAndHaploinsufficient = calledVar.isMissenseDominantAndHaploinsufficient(cCarrier);
        isKnownPathogenicVariant = calledVar.isKnownPathogenicVariant();
        isHotZone = calledVar.isHotZone();
        
        tierFlag4SingleVar = Data.BYTE_NA;

        if (calledVar.isHeterozygousTier1(cCarrier)
                || calledVar.isHomozygousTier1(cCarrier)) {
            tierFlag4SingleVar = 1;
        } else if (calledVar.isMetTier2InclusionCriteria(cCarrier)
                && calledVar.isCaseVarTier2(cCarrier)) {
            tierFlag4SingleVar = 2;
        }
        
        if (calledVar.isHeterozygousTier1(cCarrier)) {
            tierFlag4SingleVar = 1;

//            if (calledVar.getKnownVar().isOMIMDominant()) {
//                variantPrioritizationSet.add("01_TIER1_OMIM_DOM");
//            }
        } else if (calledVar.isHomozygousTier1(cCarrier)) {
            tierFlag4SingleVar = 1;

//            if (calledVar.getKnownVar().isOMIMRecessive()) {
//                variantPrioritizationSet.add("02_TIER1_OMIM_REC");
//            }
        } else if (calledVar.isMetTier2InclusionCriteria(cCarrier)
                && calledVar.isCaseVarTier2(cCarrier)) {
            tierFlag4SingleVar = 2;

//            if (cCarrier.getGT() == Index.HET && calledVar.getKnownVar().isOMIMDominant()) {
//                variantPrioritizationSet.add("03_TIER2_OMIM_DOM");
//            } else if (cCarrier.getGT() == Index.HOM && calledVar.getKnownVar().isOMIMRecessive()) {
//                variantPrioritizationSet.add("04_TIER2_OMIM_REC");
//            }
        }

        if (isLoFDominantAndHaploinsufficient == 1) {
            singleVariantPrioritizationSet.add("01_LOF_GENE");
        }

        if (tierFlag4SingleVar == 1 && cCarrier.getGT() == Index.HOM) {
            singleVariantPrioritizationSet.add("02_TIER1_HOMO_HEMI");
        }

        if (isKnownPathogenicVariant == 1) {
            singleVariantPrioritizationSet.add("03_KNOWN_VAR");
        } else {
            if (calledVar.getKnownVar().isClinVarPLPSite()) {
                singleVariantPrioritizationSet.add("04_CLINVAR_SITE");
            }

            if (calledVar.getKnownVar().isClinVar2bpFlankingValid()) {
                singleVariantPrioritizationSet.add("05_CLINVAR_2BP");
            }

            if (calledVar.getKnownVar().isHGMDDMSite()) {
                singleVariantPrioritizationSet.add("06_HGMD_SITE");
            }
        }

        if (isMissenseDominantAndHaploinsufficient == 1
                && calledVar.isClinVar25bpFlankingValid()) {
            singleVariantPrioritizationSet.add("07_MIS_HOT_SPOT");
        }

        if (tierFlag4SingleVar == 1
                && calledVar.isOMIMGene()
                && (calledVar.isMissense() || calledVar.isInframe())
                && calledVar.isGeneMisZValid()) {
            singleVariantPrioritizationSet.add("08_TIER1_OMIM_MIS_INFRAME");
        }

        if (!calledVar.getACMG().equals(Data.STRING_NA)) {
            singleVariantPrioritizationSet.add("09_ACMG_GENE");
        }
    }

    public String getSingleVariantPrioritization() {
        if (singleVariantPrioritizationSet.isEmpty()) {
            return Data.STRING_NA;
        }

        StringJoiner variantPrioritizations = new StringJoiner("|");
        Iterator itr = singleVariantPrioritizationSet.iterator();
        while (itr.hasNext()) {
            variantPrioritizations.add((String) itr.next());
        }

        return variantPrioritizations.toString();
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
        getCarrierData_pgl(sj, cCarrier, child);
//        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
