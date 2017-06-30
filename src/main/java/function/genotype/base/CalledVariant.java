package function.genotype.base;

import function.annotation.base.AnnotatedVariant;
import function.genotype.statistics.HWEExact;
import function.genotype.trio.TrioCommand;
import function.genotype.trio.TrioManager;
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
    public int[][] genoCount = new int[5][2];
    public float[] homFreq = new float[2];
    public float[] hetFreq = new float[2];
    public float[] af = new float[2];
    public double[] hweP = new double[2];
    private int[] coveredSample = new int[2];
    private double coveredSampleBinomialP;

    public CalledVariant(String chr, int variantId, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        init();
    }

    private void init() throws Exception {
        if (isValid
                && initCarrierData()) {
            DPBinBlockManager.initCarrierAndNonCarrierByDPBin(this, carrierMap, noncarrierMap);

            initGenoCovArray();

            if (checkGenoCountValid()) {
                calculateAlleleFreq();

                if (checkAlleleFreqValid()) {
                    initDPBinCoveredSampleBinomialP();

                    if (checkCoveredSampleBinomialP()) {
                        calculateGenotypeFreq();

                        calculateHweP();
                    }
                }
            }
        }
    }

    private boolean initCarrierData() {
        if (VariantManager.isUsed()) { // when --variant or --rs-number applied
            // single variant carriers data process
            CarrierBlockManager.initCarrierMap(carrierMap, this);

            if (carrierMap.isEmpty()) {
                isValid = false;
            }
        } else {
            // block variants carriers data process
            CarrierBlockManager.init(this);

            carrierMap = CarrierBlockManager.getVarCarrierMap(variantId);

            if (carrierMap == null) {
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean checkGenoCountValid() {
        int totalQCFailSample = qcFailSample[Index.CASE] + qcFailSample[Index.CTRL];

        isValid = GenotypeLevelFilterCommand.isMaxQcFailSampleValid(totalQCFailSample)
                && GenotypeLevelFilterCommand.isMinVarPresentValid(getVarPresent())
                && GenotypeLevelFilterCommand.isMinCaseCarrierValid(getCaseCarrier());

        return isValid;
    }

    private boolean checkCoveredSampleBinomialP() {
        isValid = GenotypeLevelFilterCommand
                .isMinCoveredSampleBinomialPValid(coveredSampleBinomialP);

        return isValid;
    }

    private boolean checkAlleleFreqValid() {
        isValid = GenotypeLevelFilterCommand.isMaxCtrlAFValid(af[Index.CTRL])
                && GenotypeLevelFilterCommand.isMinCtrlAFValid(af[Index.CTRL]);

        return isValid;
    }

    private int getVarPresent() {
        return genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HOM_MALE][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HOM_MALE][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
    }

    private int getCaseCarrier() {
        return genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HOM_MALE][Index.CASE]
                + genoCount[Index.HET][Index.CASE];
    }

    // initialize genotype & dpBin array for better compute performance use
    private void initGenoCovArray() {
        for (Sample sample : SampleManager.getList()) {
            Carrier carrier = carrierMap.get(sample.getId());
            NonCarrier noncarrier = noncarrierMap.get(sample.getId());

            short dpBin;

            if (carrier != null) {
                setGenoDPBin(carrier.getGT(), carrier.getDPBin(), sample.getIndex());
                addSampleGeno(carrier.getGT(), sample);

                if (carrier.getGT() == Data.BYTE_NA) {
                    // have to remove it for init Non-carrier map
                    qcFailSample[sample.getPheno()]++;
                    carrierMap.remove(sample.getId());
                }

                dpBin = applyCoverageFilter(sample,
                        carrier.getDPBin(),
                        GenotypeLevelFilterCommand.minCaseCoverageCall,
                        GenotypeLevelFilterCommand.minCtrlCoverageCall);
            } else if (noncarrier != null) {
                setGenoDPBin(noncarrier.getGT(), noncarrier.getDPBin(), sample.getIndex());
                addSampleGeno(noncarrier.getGT(), sample);

                dpBin = applyCoverageFilter(sample,
                        noncarrier.getDPBin(),
                        GenotypeLevelFilterCommand.minCaseCoverageNoCall,
                        GenotypeLevelFilterCommand.minCtrlCoverageNoCall);
            } else {
                setGenoDPBin(Data.BYTE_NA, Data.SHORT_NA, sample.getIndex());
                dpBin = Data.SHORT_NA;
            }

            if (dpBin != Data.SHORT_NA) {
                coveredSample[sample.getPheno()]++;
            }
        }

        noncarrierMap = null; // free memory
    }

    public void initDPBinCoveredSampleBinomialP() {
        if (coveredSample[Index.CASE] == 0
                || coveredSample[Index.CTRL] == 0) {
            coveredSampleBinomialP = Data.DOUBLE_NA;
        } else {
            coveredSampleBinomialP = MathManager.getBinomialTWOSIDED(coveredSample[Index.CASE] + coveredSample[Index.CTRL],
                    coveredSample[Index.CASE],
                    MathManager.devide(SampleManager.getCaseNum(), SampleManager.getTotalSampleNum()));
        }
    }

    private short applyCoverageFilter(Sample sample, short dpBin, int minCaseCov, int minCtrlCov) {
        if (sample.isCase()) // --min-case-coverage-call or --min-case-coverage-no-call
        {
            if (!GenotypeLevelFilterCommand.isMinCoverageValid(dpBin, minCaseCov)) {
                return Data.SHORT_NA;
            }
        } else // --min-ctrl-coverage-call or --min-ctrl-coverage-no-call
        {
            if (!GenotypeLevelFilterCommand.isMinCoverageValid(dpBin, minCtrlCov)) {
                return Data.SHORT_NA;
            }
        }

        return dpBin;
    }

    public void addSampleGeno(byte geno, Sample sample) {
        if (geno != Data.BYTE_NA) {
            if (TrioCommand.isListTrio && TrioManager.isParent(sample.getId())) {
                // exclude parent controls
                return;
            }

            geno = getGenotype(geno, sample);
            genoCount[geno][sample.getPheno()]++;
        }
    }

    public void deleteSampleGeno(byte geno, Sample sample) {
        if (geno != Data.BYTE_NA) {
            geno = getGenotype(geno, sample);
            genoCount[geno][sample.getPheno()]--;
        }
    }

    private byte getGenotype(byte geno, Sample sample) {
        if (sample.isMale()
                && !isInsideAutosomalOrPseudoautosomalRegions()) {

            if (geno == Index.HOM) {
                return Index.HOM_MALE;
            } else if (geno == Index.REF) {
                return Index.REF_MALE;
            }
        }

        return geno;
    }

    private void setGenoDPBin(byte geno, short bin, int s) {
        gt[s] = geno;
        dpBin[s] = bin;
    }

    public void calculate() {
        calculateAlleleFreq();

        calculateGenotypeFreq();

        calculateHweP();
    }

    private void calculateAlleleFreq() {
        int caseAC = 2 * genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HOM_MALE][Index.CASE]
                + genoCount[Index.HET][Index.CASE];
        int caseTotalAC = caseAC + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.REF][Index.CASE]
                + genoCount[Index.REF_MALE][Index.CASE];

        // (2*hom + maleHom + het) / (2*hom + maleHom + 2*het + 2*ref + maleRef)
        af[Index.CASE] = MathManager.devide(caseAC, caseTotalAC);

        int ctrlAC = 2 * genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HOM_MALE][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
        int ctrlTotalAC = ctrlAC + genoCount[Index.HET][Index.CTRL]
                + 2 * genoCount[Index.REF][Index.CTRL]
                + genoCount[Index.REF_MALE][Index.CTRL];

        af[Index.CTRL] = MathManager.devide(ctrlAC, ctrlTotalAC);
    }

    private void calculateGenotypeFreq() {
        int totalCaseGenotypeCount
                = genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HOM_MALE][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.REF][Index.CASE]
                + genoCount[Index.REF_MALE][Index.CASE];

        int totalCtrlGenotypeCount
                = genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HOM_MALE][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL]
                + genoCount[Index.REF][Index.CTRL]
                + genoCount[Index.REF_MALE][Index.CTRL];

        // hom / (hom + het + ref)
        homFreq[Index.CASE] = MathManager.devide(
                genoCount[Index.HOM][Index.CASE] + genoCount[Index.HOM_MALE][Index.CASE], totalCaseGenotypeCount);
        homFreq[Index.CTRL] = MathManager.devide(
                genoCount[Index.HOM][Index.CTRL] + genoCount[Index.HOM_MALE][Index.CTRL], totalCtrlGenotypeCount);

        hetFreq[Index.CASE] = MathManager.devide(genoCount[Index.HET][Index.CASE], totalCaseGenotypeCount);
        hetFreq[Index.CTRL] = MathManager.devide(genoCount[Index.HET][Index.CTRL], totalCtrlGenotypeCount);
    }

    private void calculateHweP() {
        hweP[Index.CASE] = HWEExact.getP(genoCount[Index.HOM][Index.CASE] + genoCount[Index.HOM_MALE][Index.CASE],
                genoCount[Index.HET][Index.CASE],
                genoCount[Index.REF][Index.CASE] + genoCount[Index.REF_MALE][Index.CASE]);

        hweP[Index.CTRL] = HWEExact.getP(genoCount[Index.HOM][Index.CTRL] + genoCount[Index.HOM_MALE][Index.CTRL],
                genoCount[Index.HET][Index.CTRL],
                genoCount[Index.REF][Index.CTRL] + genoCount[Index.REF_MALE][Index.CTRL]);
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

    public double getCoveredSampleBinomialP() {
        return coveredSampleBinomialP;
    }
}
