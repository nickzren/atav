package function.genotype.base;

import function.annotation.base.AnnotatedVariant;
import global.Data;
import global.Index;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class CalledVariant extends AnnotatedVariant {

    private HashMap<Integer, Carrier> carrierMap = new HashMap<>();
    private HashMap<Integer, NonCarrier> noncarrierMap = new HashMap<>();
    private int[] genotype = new int[SampleManager.getListSize()];
    private int[] coverage = new int[SampleManager.getListSize()];
    private int[] qcFailSample = new int[2];

    public CalledVariant(String chr, int variantId, boolean isIndel, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        init();
    }

    private void init() throws Exception {
        if (isValid) {
            SampleManager.initCarrierMap(this, carrierMap);

            CoverageBlockManager.initNonCarrierMap(this, carrierMap, noncarrierMap);

            initGenoCovArray();

            checkValid();

            noncarrierMap = null; // free memory
        }
    }

    private void checkValid() {
        int value = qcFailSample[Index.CASE] + qcFailSample[Index.CTRL];

        isValid = GenotypeLevelFilterCommand.isMaxQcFailSampleValid(value);
    }

    // initialize genotype & coverage array for better compute performance use
    private void initGenoCovArray() {
        for (int s = 0; s < SampleManager.getListSize(); s++) {
            Sample sample = SampleManager.getList().get(s);

            Carrier carrier = carrierMap.get(sample.getId());
            NonCarrier noncarrier = noncarrierMap.get(sample.getId());

            if (carrier != null) {
                setGenoCov(carrier.getGenotype(), carrier.getCoverage(), s);

                if (carrier.getGenotype() == Data.NA) {
                    // have to remove it for init Non-carrier map
                    qcFailSample[(int) sample.getPheno()]++;
                    carrierMap.remove(sample.getId());
                }
            } else if (noncarrier != null) {
                setGenoCov(noncarrier.getGenotype(), noncarrier.getCoverage(), s);
            } else {
                setGenoCov(Data.NA, Data.NA, s);
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
}
