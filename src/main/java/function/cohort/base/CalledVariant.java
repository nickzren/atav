package function.cohort.base;

import function.annotation.base.AnnotatedVariant;
import function.cohort.statistics.HWEExact;
import function.cohort.trio.TrioCommand;
import function.cohort.trio.TrioManager;
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

            if (checkGenoCountValid()) {
                calculateAlleleFreq();

                if (checkAlleleFreqValid()) {
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
                && CohortLevelFilterCommand.isMinVarPresentValid(carrierMap.size());

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
                && CohortLevelFilterCommand.isCaseAFValid(af[Index.CASE]);

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

    public void calculate() {
        calculateAlleleFreq();

        calculateGenotypeFreq();

        calculateHweP();
    }

    private void calculateAlleleFreq() {
        int caseAC = 2 * genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE];
        int caseTotalAC = caseAC + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.REF][Index.CASE];

        // (2*hom + maleHom + het) / (2*hom + maleHom + 2*het + 2*ref + maleRef)
        af[Index.CASE] = MathManager.devide(caseAC, caseTotalAC);

        int ctrlAC = 2 * genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
        int ctrlTotalAC = ctrlAC + genoCount[Index.HET][Index.CTRL]
                + 2 * genoCount[Index.REF][Index.CTRL];

        af[Index.CTRL] = MathManager.devide(ctrlAC, ctrlTotalAC);
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

    public String getGT4VCF(int index) {
        byte gt = getGT(index);

        switch (gt) {
            case Index.HOM:
                return "1/1";
            case Index.HET:
                return "1/0";
            case Index.REF:
                return "0/0";
            default:
                return "./.";
        }
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
}
