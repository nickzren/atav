package function.genotype.base;

import function.annotation.base.AnnotatedVariant;
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
    private int[] genotype = new int[SampleManager.getTotalSampleNum()];
    private int[] coverage = new int[SampleManager.getTotalSampleNum()];
    private int[] qcFailSample = new int[2];
    private int[] coveredSample = new int[2];
    private double coveredSampleBinomialP;

    public CalledVariant(int variantId, boolean isIndel, ResultSet rset) throws Exception {
        super(variantId, isIndel, rset);

        init();
    }

    private void init() throws Exception {
        if (isValid) {
            SampleManager.initCarrierMap(this, carrierMap);

            CoverageBlockManager.initNonCarrierMap(this, carrierMap, noncarrierMap);

            initGenoCovArray();

            initDPBinCoveredSampleBinomialP();

            checkValid();

            noncarrierMap = null; // free memory
        }
    }

    private void checkValid() {
        isValid = GenotypeLevelFilterCommand.isMaxQcFailSampleValid(qcFailSample[Index.CASE] + qcFailSample[Index.CTRL])
                && GenotypeLevelFilterCommand.isMinCoveredSampleBinomialPValid(coveredSampleBinomialP);
    }

    // initialize genotype & coverage array for better compute performance use
    private void initGenoCovArray() {
        for (Sample sample : SampleManager.getList()) {
            Carrier carrier = carrierMap.get(sample.getId());
            NonCarrier noncarrier = noncarrierMap.get(sample.getId());

            int dpBin;

            if (carrier != null) {
                setGenoCov(carrier.getGenotype(), carrier.getCoverage(), sample.getIndex());

                if (carrier.getGenotype() == Data.NA) {
                    // have to remove it for init Non-carrier map
                    qcFailSample[sample.getPheno()]++;
                    carrierMap.remove(sample.getId());
                }

                dpBin = applyCoverageFilter(sample,
                        carrier.getCoverage(),
                        GenotypeLevelFilterCommand.minCaseCoverageCall,
                        GenotypeLevelFilterCommand.minCtrlCoverageCall);
            } else if (noncarrier != null) {
                setGenoCov(noncarrier.getGenotype(), noncarrier.getCoverage(), sample.getIndex());

                dpBin = applyCoverageFilter(sample,
                        noncarrier.getCoverage(),
                        GenotypeLevelFilterCommand.minCaseCoverageNoCall,
                        GenotypeLevelFilterCommand.minCtrlCoverageNoCall);
            } else {
                setGenoCov(Data.NA, Data.NA, sample.getIndex());
                dpBin = Data.NA;
            }

            if (dpBin != Data.NA) {
                coveredSample[sample.getPheno()]++;
            }
        }
    }

    private void setGenoCov(int geno, int cov, int s) {
        genotype[s] = geno;
        coverage[s] = cov;
    }

    public int getCoverage(int index) {
        if (index == Data.NA) {
            return Data.NA;
        }

        return coverage[index];
    }

    public int getGenotype(int index) {
        if (index == Data.NA) {
            return Data.NA;
        }

        return genotype[index];
    }

    public Carrier getCarrier(int sampleId) {
        return carrierMap.get(sampleId);
    }

    public int getQcFailSample(int pheno) {
        return qcFailSample[pheno];
    }

    private void initDPBinCoveredSampleBinomialP() {
        coveredSampleBinomialP = MathManager.getBinomialTWOSIDED(coveredSample[Index.CASE] + coveredSample[Index.CTRL],
                coveredSample[Index.CASE],
                MathManager.devide(SampleManager.getCaseNum(), SampleManager.getTotalSampleNum()));
    }

    private int applyCoverageFilter(Sample sample, int dpBin, int minCaseCov, int minCtrlCov) {
        if (sample.isCase()) // --min-case-coverage-call or --min-case-coverage-no-call
        {
            if (!GenotypeLevelFilterCommand.isMinCoverageValid(dpBin, minCaseCov)) {
                return Data.NA;
            }
        } else // --min-ctrl-coverage-call or --min-ctrl-coverage-no-call
        {
            if (!GenotypeLevelFilterCommand.isMinCoverageValid(dpBin, minCtrlCov)) {
                return Data.NA;
            }
        }

        return dpBin;
    }

    public int getCoveredSample(int pheno) {
        return coveredSample[pheno];
    }

    public double getCoveredSampleBinomialP() {
        return coveredSampleBinomialP;
    }
}
