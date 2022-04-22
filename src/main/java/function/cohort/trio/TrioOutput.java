package function.cohort.trio;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.cohort.base.Enum.INHERITED_FROM;
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
    byte isClinGenVarLoF;
    byte isLoFdepletedpLI;

    // ACMG
    private String acmgPathogenicCriteria;
    private String acmgBenignCriteria;
    private short acmgPSCount;
    private short acmgPMCount;
    private short acmgPPCount;
    private short acmgBSCount;
    private short acmgBPCount;
    boolean isPM3 = false;
    boolean isBP2 = false;

    private LinkedHashSet<String> variantPrioritizationSet = new LinkedHashSet<>();
    private LinkedHashSet<String> bioinformaticsSignatureSet = new LinkedHashSet<>();

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
                && calledVar.isImpactHighOrModerate()
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
        if (!variantPrioritizationSet.isEmpty()) {
            return;
        }

        isHotZone = calledVar.isHotZone();
        isLoFDominantAndHaploinsufficient = calledVar.isLoFDominantAndHaploinsufficient(cCarrier);
        isKnownPathogenicVariant = calledVar.isKnownPathogenicVariant();
        isMissenseDominantAndHaploinsufficient = calledVar.isMissenseDominantAndHaploinsufficient(cCarrier);
        isClinGenVarLoF = initClinGenVarLoF4SingleVar();
        isLoFdepletedpLI = initLoFdepletedpLI4SingleVar();

        tierFlag4SingleVar = Data.BYTE_NA;

        // denovo or hom
        if (!denovoFlag.equals("NO FLAG") && !denovoFlag.equals(Data.STRING_NA)) {
            if (isDenovoTier1()) {
                if (isHotZone == 1) {
                    variantPrioritizationSet.add("01_TIER1_DNM_HZ");
                } else {
                    variantPrioritizationSet.add("02_TIER1_DNM");
                }
                tierFlag4SingleVar = 1;
            } else if (isHomozygousTier1()
                    || isHemizygousTier1()
                    || isCompoundDeletionTier1()) {
                variantPrioritizationSet.add("03_TIER1_HOMO_HEMI");
                tierFlag4SingleVar = 1;
            } else if (calledVar.isMetTier2InclusionCriteria(cCarrier)) {
                if (isDenovoTier2()) {
                    variantPrioritizationSet.add("04_TIER2_DNM");
                    tierFlag4SingleVar = 2;
                } else if (isHomozygousTier2()
                        || isHemizygousTier2()
                        || isCompoundDeletionTier2()) {
                    variantPrioritizationSet.add("05_TIER2_HOMO_HEMI");
                    tierFlag4SingleVar = 2;
                }
            }
        } else {
            if (calledVar.isMetTier2InclusionCriteria(cCarrier)
                    && calledVar.isCaseVarTier2(cCarrier)) {
                tierFlag4SingleVar = 2;
            }
        }

        if (isLoFDominantAndHaploinsufficient == 1) {
            variantPrioritizationSet.add("06_LOF_GENE");
        }

        if (isKnownPathogenicVariant == 1) {
            variantPrioritizationSet.add("07_KNOWN_VAR");
        } else {
            if (calledVar.getKnownVar().isClinVarPLPSite()) {
                variantPrioritizationSet.add("08_CLINVAR_SITE");
            }

            if (calledVar.getKnownVar().isClinVar2bpFlankingValid()) {
                variantPrioritizationSet.add("09_CLINVAR_2BP");
            }

            if (calledVar.getKnownVar().isHGMDDMSite()) {
                variantPrioritizationSet.add("10_HGMD_SITE");
            }
        }

        if (isMissenseDominantAndHaploinsufficient == 1
                && calledVar.isClinVar25bpFlankingValid()) {
            variantPrioritizationSet.add("11_MIS_HOT_SPOT");
        }

        if (tierFlag4SingleVar == 1
                && calledVar.getKnownVar().isOMIMGene()
                && (calledVar.isMissense() || calledVar.isInframe())
                && calledVar.isMissenseMisZValid()) {
            variantPrioritizationSet.add("12_TIER1_OMIM_MIS_INFRAME");
        }

        if (!calledVar.getKnownVar().getACMG().equals(Data.STRING_NA)) {
            variantPrioritizationSet.add("13_ACMG_GENE");
        }

        initBioinformaticsSignatures();
    }

    // ClinGen/Var [LoF]- A protein-truncating predicted variant found in a ClinGen defined dosage sensitive gene, 
    // or a gene where at least one ClinVar "Pathogenic" loss-of-function allele has been reported. Screen is applied across all four models DNM, HEM, HOM, CHET
    public byte initClinGenVarLoF4SingleVar() {
        if (calledVar.isLOF()) {
            if (denovoFlag.contains("DE NOVO")) {
                if (calledVar.getKnownVar().isInClinGenSufficientOrSomeEvidence()
                        || calledVar.isInClinVarPathoratio()) {
                    return 1;
                }
            } else if (denovoFlag.contains("HOMOZYGOUS")
                    || denovoFlag.contains("HEMIZYGOUS")) {
                if (calledVar.getKnownVar().isInClinGenRecessiveEvidence()
                        || calledVar.isInClinVarPathoratio()) {
                    return 1;
                }
            }
        }

        return 0;
    }

    // A protein-truncating predicted de novo allele found in a gene reported to be loss-of-function depleted (FDR<0.01, Petrovski et al.), 
    // or defined as LoF intolerant based on the ExAC paper (p>0.9). Restricted to de novo mutations. For recessive genotypes (HEM, HOM, CHET), pLI/pREC>0.9.
    public byte initLoFdepletedpLI4SingleVar() {
        if (calledVar.isLOF()) {
            if (denovoFlag.contains("DE NOVO")) {
                if (calledVar.isFDRValid() || calledVar.isPLIValid()) {
                    return 1;
                }
            } else if (denovoFlag.contains("HOMOZYGOUS")
                    || denovoFlag.contains("HEMIZYGOUS")) {
                if (calledVar.isPLIValid() || calledVar.isPRECValid()) {
                    return 1;
                }
            }
        }

        return 0;
    }

    public void initClinGenVarLoF4CHET() {
        if (isClinGenVarLoF == 0
                && calledVar.isLOF()
                && (calledVar.getKnownVar().isInClinGenRecessiveEvidence()
                || calledVar.isInClinVarPathoratio())) {
            isClinGenVarLoF = 1;
        }
    }

    public void initLoFdepletedpLI4CHET() {
        if (isLoFdepletedpLI == 0
                && calledVar.isLOF()
                && (calledVar.isPLIValid() || calledVar.isPRECValid())) {
            isLoFdepletedpLI = 1;
        }
    }

    public void initBioinformaticsSignatures() {
        if (denovoFlag.contains("DE NOVO")) {
            bioinformaticsSignatureSet.add("DNM");
        } else if (denovoFlag.contains("HOMOZYGOUS")) {
            bioinformaticsSignatureSet.add("HOMO");
        } else if (denovoFlag.contains("HEMIZYGOUS")) {
            bioinformaticsSignatureSet.add("HEMI");
        } else if (denovoFlag.contains("COMPOUND DELETION")) {
            bioinformaticsSignatureSet.add("C_DEL");
        }

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

        if (tierFlag4SingleVar == 1
                && calledVar.getKnownVar().isOMIMGene()
                && (calledVar.isMissense() || calledVar.isInframe())
                && calledVar.isMissenseMisZValid()) {
            bioinformaticsSignatureSet.add("TIER1_OMIM_MIS_INFRAME");
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
        if (variantPrioritizationSet.isEmpty()) {
            return Data.STRING_NA;
        }

        StringJoiner variantPrioritizations = new StringJoiner("|");
        Iterator itr = variantPrioritizationSet.iterator();
        while (itr.hasNext()) {
            variantPrioritizations.add((String) itr.next());
        }

        return variantPrioritizations.toString();
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
        if (calledVar.isPS2(!isDUO, denovoFlag)) {
            sj.add("PS2");
            acmgPSCount++;
        }

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

        if (isPM3) {
            sj.add("PM3");
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

        if (calledVar.isPM6(!isDUO, denovoFlag)) {
            sj.add("PM6");
            acmgPMCount++;
        }

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
        sj.add(isDUO ? "DUO" : "TRIO");
        if (!denovoFlag.equals("NO FLAG") && !denovoFlag.equals(Data.STRING_NA)) {
            sj.add(denovoFlag);
        }
        if (getInheritedFrom() != INHERITED_FROM.NA) {
            sj.add("Inherited from " + getInheritedFrom().name());
        }
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
        sj.add(FormatManager.getInteger(
                denovoFlag.contains("DE NOVO")
                && isHotZone == 1
                && calledVar.getMgi().split(",")[1].equals("1") ? 1 : 0));
        sj.add(FormatManager.getInteger(isClinGenVarLoF));
        sj.add(FormatManager.getInteger(isLoFdepletedpLI));
        sj.add(getACMGClassification());
        sj.add(getACMGPathogenicCriteria());
        sj.add(getACMGBenignCriteria());
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
