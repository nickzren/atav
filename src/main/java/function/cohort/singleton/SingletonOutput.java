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

    // ACMG
    private String acmgPathogenicCriteria;
    private String acmgBenignCriteria;
    private int acmgPSCount;
    private int acmgPMCount;
    private int acmgPPCount;
    private int acmgBSCount;
    private int acmgBPCount;
    boolean isPM3 = false;
    boolean isBP2 = false;

    private String variantPrioritization;
    private LinkedHashSet<String> bioinformaticsSignatureSet = new LinkedHashSet<>();

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.add("Proband");
        sj.add("Ancestry");
        sj.add("Broad Phenotype");
        sj.add("Gene Name");
        sj.add("Gene Link");
//        sj.add("ACMG Classification");
//        sj.add("ACMG Pathogenic Criteria");
//        sj.add("ACMG Benign Criteria");
        sj.add("Variant Prioritization");
        sj.add("Bioinformatics Signatures");
        sj.add("Compound Var");
        sj.add("Var Ctrl Freq #1 & #2 (co-occurance)");
        sj.add("Tier Flag (Compound Var)");
        sj.add("Tier Flag (Single Var)");
        sj.add("Pass Tier 2 Inclusion Criteria");
        sj.add("LoF Dominant and Haploinsufficient Gene");
        sj.add("Known Pathogenic Variant");
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

        variantPrioritization = "";
    }

    public void initSingletonData(Singleton singleton) {
        child = singleton.getChild();
        cGeno = calledVar.getGT(child.getIndex());
        cDPBin = calledVar.getDPBin(child.getIndex());
        cCarrier = calledVar.getCarrier(singleton.getChild().getId());
    }

    public void initTierFlag4SingleVar() {
        if (!variantPrioritization.isEmpty()) {
            return;
        }

        isLoFDominantAndHaploinsufficient = calledVar.isLoFDominantAndHaploinsufficient(cCarrier);
        isMissenseDominantAndHaploinsufficient = calledVar.isMissenseDominantAndHaploinsufficient(cCarrier);
        isKnownPathogenicVariant = calledVar.isKnownPathogenicVariant();
        isHotZone = calledVar.isHotZone();

        tierFlag4SingleVar = Data.BYTE_NA;
        if (calledVar.isHeterozygousTier1(cCarrier)) {
            tierFlag4SingleVar = 1;

            if (calledVar.getKnownVar().isOMIMDominant()) {
//                setVariantPrioritization("01_TIER1_OMIM_DOM");
            }
        } else if (calledVar.isHomozygousTier1(cCarrier)) {
            tierFlag4SingleVar = 1;

            if (calledVar.getKnownVar().isOMIMRecessive()) {
//                setVariantPrioritization("02_TIER1_OMIM_REC");
            }
        } else if (calledVar.isMetTier2InclusionCriteria(cCarrier)
                && calledVar.isCaseVarTier2(cCarrier)) {
            tierFlag4SingleVar = 2;

            if (cCarrier.getGT() == Index.HET && calledVar.getKnownVar().isOMIMDominant()) {
//                setVariantPrioritization("03_TIER2_OMIM_DOM");
            } else if (cCarrier.getGT() == Index.HOM && calledVar.getKnownVar().isOMIMRecessive()) {
//                setVariantPrioritization("04_TIER2_OMIM_REC");
            }
        }

        if (isLoFDominantAndHaploinsufficient == 1) {
            setVariantPrioritization("01_LOF_GENE");
        }

        if (tierFlag4SingleVar == 1 && cCarrier.getGT() == Index.HOM) {
            setVariantPrioritization("02_TIER1_HOMO_HEMI");
        }

        if (isKnownPathogenicVariant == 1) {
            setVariantPrioritization("03_KNOWN_VAR");
        } else {
            if (calledVar.getKnownVar().isClinVarPLPSite()) {
                setVariantPrioritization("04_CLINVAR_SITE");
            }

            if (calledVar.getKnownVar().isClinVar2bpFlankingValid()) {
                setVariantPrioritization("05_CLINVAR_2BP");
            }

            if (calledVar.getKnownVar().isHGMDDMSite()) {
                setVariantPrioritization("06_HGMD_SITE");
            }
        }

        if (isMissenseDominantAndHaploinsufficient == 1
                && calledVar.isClinVar25bpFlankingValid()) {
            setVariantPrioritization("07_MISSENSE_HS");
        }

        if (tierFlag4SingleVar == 1
                && (calledVar.isMissense() || calledVar.isInframe())
                && calledVar.isMissenseMisZValid()) {
            setVariantPrioritization("08_MIS_INFRAME");
        }

        if (!calledVar.getKnownVar().getACMG().equals(Data.STRING_NA)) {
            setVariantPrioritization("09_ACMG_GENE");
        }
        
        initBioinformaticsSignatures();
    }

    public void setVariantPrioritization(String str) {
        if (variantPrioritization.isEmpty()) {
            variantPrioritization = str;
        }
    }

    public void initBioinformaticsSignatures() {
        if (calledVar.isGenotypeAbsentAmongControl(cCarrier.getGT())) {
            bioinformaticsSignatureSet.add("ULTRA_RARE");
        }

        if (calledVar.isLOF()) {
            bioinformaticsSignatureSet.add("LOF_VAR");
        }

        if (calledVar.hasCCDS()) {
            bioinformaticsSignatureSet.add("CCDS");
        }

        if (cCarrier.getGT() == Index.HET) {
            if (calledVar.getKnownVar().isInClinGenSufficientOrSomeEvidence()) {
                bioinformaticsSignatureSet.add("CLINGEN_GENE");
            }

            if (calledVar.getKnownVar().isOMIMDominant()) {
                bioinformaticsSignatureSet.add("OMIM_GENE");
            }
        } else if (cCarrier.getGT() == Index.HOM) {
            if (calledVar.getKnownVar().isInClinGenRecessiveEvidence()) {
                bioinformaticsSignatureSet.add("CLINGEN_GENE");
            }

            if (calledVar.getKnownVar().isOMIMRecessive()) {
                bioinformaticsSignatureSet.add("OMIM_GENE");
            }
        }

        if (!calledVar.getKnownVar().getACMG().equals(Data.STRING_NA)) {
            bioinformaticsSignatureSet.add("ACMG_GENE");
        }

        if (calledVar.getMgi().split(",")[1].equals("1")) {
            bioinformaticsSignatureSet.add("MGI_ESSENTIAL");
        }

        if (calledVar.getKnownVar().isClinVarPLP()) {
            bioinformaticsSignatureSet.add("CLINVAR_PLP");
        } else if (calledVar.getKnownVar().isClinVarPLPSite()) {
            bioinformaticsSignatureSet.add("CLINVAR_PLP_SITE");
        } else if (calledVar.getKnownVar().isClinVar2bpFlankingValid()) {
            bioinformaticsSignatureSet.add("CLINVAR_PLP_2BP");
        }

        if (calledVar.isInClinVarPathoratio()) {
            bioinformaticsSignatureSet.add("CLINVAR_PATHORATIO");
        }

        if (calledVar.getKnownVar().isHGMDDM()) {
            bioinformaticsSignatureSet.add("HGMD_DM");
        } else if (calledVar.getKnownVar().isHGMDDMSite()) {
            bioinformaticsSignatureSet.add("HGMD_DM_SITE");
        } else if (calledVar.getKnownVar().isHGMD2bpFlankingValid()) {
            bioinformaticsSignatureSet.add("HGMD_DM_2BP");
        }

        if (isHotZone == 1) {
            bioinformaticsSignatureSet.add("HOT_ZONE");
        }

        if (isMissenseDominantAndHaploinsufficient == 1
                && calledVar.isClinVar25bpFlankingValid()) {
            bioinformaticsSignatureSet.add("MIS_HOT_SPOT");
        }

        if (calledVar.isLoFPLIValid()) {
            bioinformaticsSignatureSet.add("PLI");
        }

        if (calledVar.isMissenseMisZValid()) {
            bioinformaticsSignatureSet.add("MIS_Z");
        }

        if (calledVar.isRepeatRegion()) {
            bioinformaticsSignatureSet.add("REPEAT_REGION");
        }
    }
    
    public String getVariantPrioritization() {
        if (variantPrioritization.isEmpty()) {
            return Data.STRING_NA;
        }

        return variantPrioritization;
    }

    public String getBioinformaticsSignatures() {
        if (bioinformaticsSignatureSet.isEmpty()) {
            return Data.STRING_NA;
        }

        StringJoiner bioinformaticsSignatures = new StringJoiner("|");
        Iterator itr = bioinformaticsSignatureSet.iterator();
        while (itr.hasNext()) {
            bioinformaticsSignatures.add((String) itr.next());
        }

        return bioinformaticsSignatures.toString();
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

        if (isKnownPathogenicVariant == 1) {
            Output.knownPathogenicVarCount++;
        }
    }

    public String getACMGClassification() {
        boolean isPathogenic = false;
        boolean isLikelyPathogenic = false;
        boolean isBenign = false;
        boolean isLikeBenign = false;

        boolean isPVS1 = calledVar.isPVS1(cCarrier);

        if (isPVS1
                && (acmgPSCount >= 1
                || acmgPMCount >= 2
                || (acmgPMCount == 1 && acmgPPCount == 1)
                || acmgPPCount >= 2)) {
            isPathogenic = true;
        }

        if (acmgPSCount >= 2) {
            isPathogenic = true;
        }

        if (acmgPSCount == 1
                && (acmgPMCount >= 3
                || (acmgPMCount == 2 && acmgPPCount >= 2)
                || (acmgPMCount == 1 && acmgPPCount >= 4))) {
            isPathogenic = true;
        }

        if ((isPVS1 && acmgPMCount == 1)
                || (acmgPSCount == 1 && acmgPMCount >= 1)
                || (acmgPSCount == 1 && acmgPPCount >= 2)
                || acmgPMCount >= 3
                || (acmgPMCount == 2 && acmgPPCount >= 2)
                || (acmgPMCount == 1 && acmgPPCount >= 4)) {
            isLikelyPathogenic = true;
        }

        if (calledVar.isBA1() || acmgBSCount >= 2) {
            isBenign = true;
        }

        if ((acmgBSCount == 1 && acmgBPCount == 1)
                || acmgBPCount >= 2) {
            isLikeBenign = true;
        }

        if ((!isPathogenic && !isLikelyPathogenic && !isBenign && !isLikeBenign) // Other criteria shown above are not met
                || ((isPathogenic || isLikelyPathogenic)) && (isBenign || isLikeBenign) // the criteria for benign and pathogenic are contradictory
                ) {
            return "Uncertain significance";
        }

        if (isPathogenic) {
            return "Pathogenic";
        } else if (isLikelyPathogenic) {
            return "Likely pathogenic";
        } else if (isBenign) {
            return "Benign";
        } else if (isLikeBenign) {
            return "Like benign";
        } else {
            return "Uncertain significance";
        }
    }

    public void initACMG() {
        acmgPSCount = 0;
        acmgPMCount = 0;
        acmgPPCount = 0;
        acmgBSCount = 0;
        acmgBPCount = 0;

        initACMGPathogenicCriteria();
        initACMGBenignCriteria();
    }

    private void initACMGPathogenicCriteria() {
        StringJoiner sj = new StringJoiner("|");

        if (calledVar.isPVS1(cCarrier)) {
            sj.add("PVS1");
        }

        // PS1 not clear
        // Same amino acid change as a previously established pathogenic variant regardless of nucleotide change
        //
        // PS2 not support
        // Denovo (TRIO)
        //
        // PS3 not clear
        // Well-established in vitro or in vivo functional studies supportive of a damaging effect on the gene or gene product
        //
        // PS4 not clear
        // The prevalence of the variant in affected individuals is significantly increased compared to the prevalence in controls
        //
        if (calledVar.isPM1()) {
            sj.add("PM1");
            acmgPMCount++;
        }

        if (calledVar.isPM2(cCarrier)) {
            sj.add("PM2");
            acmgPMCount++;
        }

        if (calledVar.isPM4()) {
            sj.add("PM4");
            acmgPMCount++;
        }

        if (calledVar.isPM5()) {
            sj.add("PM5");
            acmgPMCount++;
        }

        // PM6 not support
        // Denovo (DUO)
        //
        // PP1 not clear
        // Co-segregation with disease in multiple affected family members in a gene definitively known to cause the disease
        //
        if (calledVar.isPP2()) {
            sj.add("PP2");
            acmgPPCount++;
        }

        if (calledVar.isPP3()) {
            sj.add("PP3");
            acmgPPCount++;
        }

        // PP4 not clear
        // Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology
        if (calledVar.isPP5()) {
            sj.add("PP5");
            acmgPPCount++;
        }

        acmgPathogenicCriteria = sj.toString();

        if (acmgPathogenicCriteria.isEmpty()) {
            acmgPathogenicCriteria = Data.STRING_NA;
        }
    }

    public String getACMGPathogenicCriteria() {
        return acmgPathogenicCriteria;
    }

    private void initACMGBenignCriteria() {
        StringJoiner sj = new StringJoiner("|");

        if (calledVar.isBA1()) {
            sj.add("BA1");
        }

        if (calledVar.isBS1()) {
            sj.add("BS1");
            acmgBSCount++;
        }

        if (calledVar.isBS2()) {
            sj.add("BS2");
            acmgBSCount++;
        }

        // BS3 not clear
        // Well-established in vitro or in vivo functional studies shows no damaging effect on protein function or splicing
        //
        // BS4 not clear
        // Lack of segregation in affected members of a family
        //
        if (calledVar.isBP1()) {
            sj.add("BP1");
            acmgBPCount++;
        }

        // BP2 not clear
        // Observed in trans with a pathogenic variant for a fully penetrant dominant gene/disorder; or observed in cis with a pathogenic variant in any inheritance pattern
        //
        if (calledVar.isBP3()) {
            sj.add("BP3");
            acmgBPCount++;
        }

        if (calledVar.isBP4()) {
            sj.add("BP4");
            acmgBPCount++;
        }

        // BP5 not clear
        // Variant found in a case with an alternate molecular basis for disease
        //
        if (calledVar.isBP6()) {
            sj.add("BP6");
            acmgBPCount++;
        }

        if (calledVar.isBP7()) {
            sj.add("BP7");
            acmgBPCount++;
        }

        acmgBenignCriteria = sj.toString();

        if (acmgBenignCriteria.isEmpty()) {
            acmgBenignCriteria = Data.STRING_NA;
        }
    }

    public String getACMGBenignCriteria() {
        return acmgBenignCriteria;
    }

    public String getSummary() {
        StringJoiner sj = new StringJoiner("\n");

        sj.add(calledVar.getGeneName());
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
        sj.add(FormatManager.getByte(isKnownPathogenicVariant));
        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, cCarrier, child);
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
