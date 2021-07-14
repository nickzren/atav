package function.cohort.base;

import function.annotation.base.AnnotatedVariant;
import function.cohort.statistics.HWEExact;
import function.cohort.trio.TrioCommand;
import function.cohort.trio.TrioManager;
import function.variant.base.Output;
import function.variant.base.VariantManager;
import global.Data;
import global.Index;
import java.sql.ResultSet;
import java.util.HashMap;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class CalledVariant extends AnnotatedVariant {

    private HashMap<Integer, Carrier> carrierMap = new HashMap<>();
    private HashMap<Integer, NonCarrier> noncarrierMap = new HashMap<>();
    private byte[] gt = new byte[SampleManager.getTotalSampleNum()];
    private short[] dpBin = new short[SampleManager.getTotalSampleNum()];

    private int[] qcFailSample = new int[2];
    public int[][] genoCount = new int[3][2];
    public float[] homFreq = new float[2];
    public float[] hetFreq = new float[2];
    public int ac;
    public float[] af = new float[3];
    public double[] hweP = new double[2];
    private int[] coveredSample = new int[2];
    private double coveredSampleBinomialP;
    private float[] coveredSamplePercentage = new float[2];

    public CalledVariant(String chr, int variantId, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        init();
    }

    private void init() throws Exception {
        if (isValid
                && initCarrierData()) {
            DPBinBlockManager.initCarrierAndNonCarrierByDPBin(this, carrierMap, noncarrierMap);

            initGenoCovArray();

            calculateAlleleFreq();

            if (checkGenoCountValid()
                    && checkAlleleFreqValid()
                    && checkLooAFValid()) {
                switchGT();

                initDPBinCoveredSampleBinomialP();

                initCoveredSamplePercentage();

                if (checkCoveredSampleBinomialP()
                        && checkCoveredSamplePercentage()) {
                    calculateGenotypeFreq();

                    calculateHweP();
                }
            }
        }
    }

    private boolean initCarrierData() {
        if (VariantManager.isUsed()) { // when --variant or --rs-number applied
            // single variant carriers data process
            CarrierBlockManager.initCarrierMap(carrierMap, this);

            if (!CohortLevelFilterCommand.isMinVarPresentValid(carrierMap.size())) {
                isValid = false;
            }
        } else {
            // block variants carriers data process
            CarrierBlockManager.init(this);

            carrierMap = CarrierBlockManager.getVarCarrierMap(variantId);

            if (carrierMap == null) {
                carrierMap = new HashMap<>();

                if (CohortLevelFilterCommand.minVarPresent > 0) {
                    isValid = false;
                }
            } else if (!CohortLevelFilterCommand.isMinVarPresentValid(carrierMap.size())) {
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean checkGenoCountValid() {
        int totalQCFailSample = qcFailSample[Index.CASE] + qcFailSample[Index.CTRL];

        isValid = CohortLevelFilterCommand.isMaxQcFailSampleValid(totalQCFailSample)
                && CohortLevelFilterCommand.isMinCaseCarrierValid(getCaseCarrier())
                && CohortLevelFilterCommand.isMinVarPresentValid(carrierMap.size())
                && CohortLevelFilterCommand.isACValid(ac);

        return isValid;
    }

    private boolean checkCoveredSampleBinomialP() {
        isValid = CohortLevelFilterCommand.isMinCoveredSampleBinomialPValid(coveredSampleBinomialP);

        return isValid;
    }

    private boolean checkCoveredSamplePercentage() {
        isValid = CohortLevelFilterCommand.isMinCoveredCasePercentageValid(coveredSamplePercentage[Index.CASE])
                && CohortLevelFilterCommand.isMinCoveredCtrlPercentageValid(coveredSamplePercentage[Index.CTRL]);

        return isValid;
    }

    private boolean checkAlleleFreqValid() {
        isValid = CohortLevelFilterCommand.isCtrlAFValid(af[Index.CTRL])
                && CohortLevelFilterCommand.isCaseAFValid(af[Index.CASE])
                && CohortLevelFilterCommand.isAFValid(af[Index.ALL]);

        return isValid;
    }

    private int getCaseCarrier() {
        return genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE];
    }

    // initialize genotype & dpBin array for better compute performance use
    private void initGenoCovArray() {
        for (Sample sample : SampleManager.getList()) {
            Carrier carrier = carrierMap.get(sample.getId());
            NonCarrier noncarrier = noncarrierMap.get(sample.getId());

            boolean isCoveredSampleValid;

            if (carrier != null) {
                isCoveredSampleValid = GenotypeLevelFilterCommand.isMinDpBinValid(carrier.getDPBin());

                if (!isCoveredSampleValid) {
                    carrier.setGT(Data.BYTE_NA);
                    carrier.setDPBin(Data.SHORT_NA);
                }

                setGenoDPBin(carrier.getGT(), carrier.getDPBin(), sample.getIndex());
                addSampleGeno(carrier.getGT(), sample);

                if (carrier.getGT() == Data.BYTE_NA) {
                    // have to remove it for init Non-carrier map
                    qcFailSample[sample.getPheno()]++;
                    carrierMap.remove(sample.getId());
                }

            } else if (noncarrier != null) {
                isCoveredSampleValid = GenotypeLevelFilterCommand.isMinDpBinValid(noncarrier.getDPBin());

                if (!isCoveredSampleValid) {
                    noncarrier.setGT(Data.BYTE_NA);
                    noncarrier.setDPBin(Data.SHORT_NA);
                }

                setGenoDPBin(noncarrier.getGT(), noncarrier.getDPBin(), sample.getIndex());
                addSampleGeno(noncarrier.getGT(), sample);
            } else {
                setGenoDPBin(Data.BYTE_NA, Data.SHORT_NA, sample.getIndex());
                isCoveredSampleValid = false;
            }

            if (isCoveredSampleValid) {
                coveredSample[sample.getPheno()]++;
            }
        }

        noncarrierMap = null; // free memory
    }

    public void initDPBinCoveredSampleBinomialP() {
        if (CohortLevelFilterCommand.minCoveredSampleBinomialP != Data.NO_FILTER) {
            if (coveredSample[Index.CASE] == 0
                    || coveredSample[Index.CTRL] == 0) {
                coveredSampleBinomialP = Data.DOUBLE_NA;
            } else {
                coveredSampleBinomialP = MathManager.getBinomialTWOSIDED(coveredSample[Index.CASE] + coveredSample[Index.CTRL],
                        coveredSample[Index.CASE],
                        MathManager.devide(SampleManager.getCaseNum(), SampleManager.getTotalSampleNum()));
            }
        }
    }

    public void initCoveredSamplePercentage() {
        coveredSamplePercentage[Index.CASE] = MathManager.devide(coveredSample[Index.CASE], SampleManager.getCaseNum()) * 100;
        coveredSamplePercentage[Index.CTRL] = MathManager.devide(coveredSample[Index.CTRL], SampleManager.getCtrlNum()) * 100;
    }

    public void addSampleGeno(byte geno, Sample sample) {
        if (geno != Data.BYTE_NA) {
            if (TrioCommand.isList && TrioManager.isParent(sample.getId())) {
                // exclude parent controls
                return;
            }

            genoCount[geno][sample.getPheno()]++;
        }
    }

    public void deleteSampleGeno(byte geno, Sample sample) {
        if (geno != Data.BYTE_NA) {
            genoCount[geno][sample.getPheno()]--;
        }
    }

    private void setGenoDPBin(byte geno, short bin, int s) {
        gt[s] = geno;
        dpBin[s] = bin;
    }

    // --max-ctrl-maf or --max-loo-maf or --max-maf will tigger to swich GT when its AF > 0.5
    private void switchGT() {
        if ((CohortLevelFilterCommand.maxCtrlMAF != Data.NO_FILTER && af[Index.CTRL] > 0.5)
                || (CohortLevelFilterCommand.maxLooMAF != Data.NO_FILTER && af[Index.ALL] > 0.5)
                || (CohortLevelFilterCommand.maxMAF != Data.NO_FILTER && af[Index.ALL] > 0.5)) {
            // switch per sample GT
            for (int s = 0; s < SampleManager.getList().size(); s++) {
                if (gt[s] == Index.REF) {
                    gt[s] = Index.HOM;
                } else if (gt[s] == Index.HOM) {
                    gt[s] = Index.REF;
                }
            }

            // switch sample GT count
            int homCtrl = genoCount[Index.HOM][Index.CTRL];
            genoCount[Index.HOM][Index.CTRL] = genoCount[Index.REF][Index.CTRL];
            genoCount[Index.REF][Index.CTRL] = homCtrl;

            int homCase = genoCount[Index.HOM][Index.CASE];
            genoCount[Index.HOM][Index.CASE] = genoCount[Index.REF][Index.CASE];
            genoCount[Index.REF][Index.CASE] = homCase;
        }
    }

    // af = (2*hom + het) / (2*hom + 2*het + 2*ref)
    private void calculateAlleleFreq() {
        // case af
        int caseAC = 2 * genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE];
        int caseTotalAC = caseAC + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.REF][Index.CASE];
        af[Index.CASE] = MathManager.devide(caseAC, caseTotalAC);

        // ctrl af
        int ctrlAC = 2 * genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
        int ctrlTotalAC = ctrlAC + genoCount[Index.HET][Index.CTRL]
                + 2 * genoCount[Index.REF][Index.CTRL];
        af[Index.CTRL] = MathManager.devide(ctrlAC, ctrlTotalAC);

        // all af
        ac = ctrlAC + caseAC;
        af[Index.ALL] = MathManager.devide(ac, ctrlTotalAC + caseTotalAC);
    }

    private boolean checkLooAFValid() {
        if(CohortLevelFilterCommand.maxLooMAF == Data.NO_FILTER
                && CohortLevelFilterCommand.maxLooAF == Data.NO_FILTER) {
            return true;
        }
        
        for (Carrier carrier : carrierMap.values()) {
            byte geno = carrier.gt;
            Sample sample = SampleManager.getMap().get(carrier.getSampleId());

            // delete current sample geno as 'leave one out' concept
            deleteSampleGeno(geno, sample);

            // calculateLooAF
            int alleleCount = 2 * genoCount[Index.HOM][Index.CASE]
                    + genoCount[Index.HET][Index.CASE]
                    + 2 * genoCount[Index.HOM][Index.CTRL]
                    + genoCount[Index.HET][Index.CTRL];
            int totalCount = alleleCount
                    + genoCount[Index.HET][Index.CASE]
                    + 2 * genoCount[Index.REF][Index.CASE]
                    + genoCount[Index.HET][Index.CTRL]
                    + 2 * genoCount[Index.REF][Index.CTRL];

            float looAF = MathManager.devide(alleleCount, totalCount);

            // add deleted sample geno back
            addSampleGeno(geno, sample);
            
            // if any samples' loo af failed to pass the threshold, set variant to invalid
            if (!CohortLevelFilterCommand.isLooAFValid(looAF)) {
                isValid = false;
                break;
            }

        }

        return isValid;
    }

    private void calculateGenotypeFreq() {
        int totalCaseGenotypeCount
                = genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.REF][Index.CASE];

        int totalCtrlGenotypeCount
                = genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL]
                + genoCount[Index.REF][Index.CTRL];

        // hom / (hom + het + ref)
        homFreq[Index.CASE] = MathManager.devide(
                genoCount[Index.HOM][Index.CASE], totalCaseGenotypeCount);
        homFreq[Index.CTRL] = MathManager.devide(
                genoCount[Index.HOM][Index.CTRL], totalCtrlGenotypeCount);

        hetFreq[Index.CASE] = MathManager.devide(genoCount[Index.HET][Index.CASE], totalCaseGenotypeCount);
        hetFreq[Index.CTRL] = MathManager.devide(genoCount[Index.HET][Index.CTRL], totalCtrlGenotypeCount);
    }

    private void calculateHweP() {
        hweP[Index.CASE] = HWEExact.getP(genoCount[Index.HOM][Index.CASE],
                genoCount[Index.HET][Index.CASE],
                genoCount[Index.REF][Index.CASE]);

        hweP[Index.CTRL] = HWEExact.getP(genoCount[Index.HOM][Index.CTRL],
                genoCount[Index.HET][Index.CTRL],
                genoCount[Index.REF][Index.CTRL]);
    }

    public short getDPBin(int index) {
        if (index == Data.INTEGER_NA) {
            return Data.SHORT_NA;
        }

        return dpBin[index];
    }

    public byte getGT(int index) {
        if (index == Data.INTEGER_NA) {
            return Data.BYTE_NA;
        }

        return gt[index];
    }

    public Carrier getCarrier(int sampleId) {
        return carrierMap.get(sampleId);
    }

    public int getQcFailSample(byte pheno) {
        return qcFailSample[pheno];
    }

    public int getCoveredSample(byte pheno) {
        return coveredSample[pheno];
    }

    public float getCoveredSamplePercentage(byte pheno) {
        return coveredSamplePercentage[pheno];
    }

    public double getCoveredSampleBinomialP() {
        return coveredSampleBinomialP;
    }

    // NS = Number of Samples With Data
    public int getNS() {
        return genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.REF][Index.CASE]
                + genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL]
                + genoCount[Index.REF][Index.CTRL];
    }

    public int getAC() {
        return ac;
    }

    public int getAN() {
        return getAC()
                + 2 * genoCount[Index.REF][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.REF][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
    }

    // NHOM = Number of homozygotes
    public int getNHOM() {
        return genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HOM][Index.CASE];
    }

    // AF = Allele Frequency
    public float getAF() {
        int ac = 2 * genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
        int totalAC = ac + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.REF][Index.CASE]
                + genoCount[Index.HET][Index.CTRL]
                + 2 * genoCount[Index.REF][Index.CTRL];

        return MathManager.devide(ac, totalAC);
    }
    
    /*
        1. LoF variant and occurs witin a ClinGen gene with "Sufficient" or "Some" evidence
        2. variant is het call and <= 5 observed among IGM and gnomAD controls
     */
    public byte isDominantAndHaploinsufficient(Carrier carrier) {
        if (isLOF()
                && (getClinGen().isInClinGenSufficientOrSomeEvidence()
                || isOMIMDominant()) // 1
                && carrier.getGT() == Index.HET && isNHetFromControlsValid(5) // 2
                ) {
            Output.dominantAndHaploinsufficientCount++;
            return 1;
        }

        return 0;
    }

    /*
        same variant curated as "DM" in HGMD or PLP in ClinVar
     */
    public byte isPreviouslyPathogenicReported(Carrier carrier) {
        if (getKnownVar().isHGMDDM() || getKnownVar().isClinVarPLP()) {
            Output.previouslyPathogenicReportedCount++;
            return 1;
        }

        return 0;
    }

    // genotype is absent among IGM controls and gnomAD (WES & WGS) controls
    public boolean isGenotypeAbsentAmongControl(int gt) {
        if (gt == Index.HET) {
            return isNHetFromControlsValid(0);
        } else { // HOM Alt
            return isNHomFromControlsValid(0);
        }
    }

    /*
        1. het carrier >= 10% percent alt read OR hom carrier >= 80% percent alt read
        2. Qual >= 50, QD >= 2, MQ >= 40 
        3. genotype is absent among IGM and gnomAD controls
     */
    public boolean isCaseVarTier1(Carrier carrier) {
        return (isCarrierHetPercAltReadValid(carrier)
                || isCarrieHomPercAltReadValid(carrier))
                && isCarrierGATKQCValid(carrier)
                && isGenotypeAbsentAmongControl(carrier.getGT());
    }

    // het carrier and >= 10% percent alt read
    public boolean isCarrierHetPercAltReadValid(Carrier carrier) {
        if (carrier.getGT() == Index.HET) {
            float percAltRead = carrier != null ? carrier.getPercAltRead() : Data.FLOAT_NA;

            return percAltRead != Data.FLOAT_NA && percAltRead >= 0.1;
        }

        return false;
    }

    // hom carrier and >= 80% percent alt read
    public boolean isCarrieHomPercAltReadValid(Carrier carrier) {
        if (carrier.getGT() == Index.HOM) {
            float percAltRead = carrier != null ? carrier.getPercAltRead() : Data.FLOAT_NA;

            return percAltRead != Data.FLOAT_NA && percAltRead >= 0.8;
        }

        return false;
    }

    // DP bin >= 10, Qual >= 50, QD >= 2, MQ >= 40
    public boolean isCarrierGATKQCValid(Carrier carrier) {
        if (carrier != null) {
            return carrier.getDPBin() >= 10
                    && carrier.getQual() >= 50
                    && carrier.getQD() >= 2
                    && carrier.getMQ() >= 40;
        }

        return false;
    }

    public boolean isCaseVarTier2() {
        return isTotalACFromControlsValid();
    }
}
