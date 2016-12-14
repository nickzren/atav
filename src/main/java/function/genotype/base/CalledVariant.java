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
    private int[] gt = new int[SampleManager.getListSize()];
    private int[] dpBin = new int[SampleManager.getListSize()];
    private int[] qcFailSample = new int[2];

    public CalledVariant(String chr, int variantId, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        init();
    }

    private void init() throws Exception {
        if (isValid) {
            // single variant carriers data process
//            CarrierBlockManager.initCarrierMap(carrierMap, this);
//
//            if (carrierMap.isEmpty()) {
//                isValid = false;
//                return;
//            }

            // block variants carriers data process
            CarrierBlockManager.init(this);
            
            carrierMap = CarrierBlockManager.getVarCarrierMap(variantId);
            
            if (carrierMap == null) {
                isValid = false;
                return;
            }

            DPBinBlockManager.initCarrierAndNonCarrierByDPBin(this, carrierMap, noncarrierMap);

            initGenoCovArray();

            checkValid();

            noncarrierMap = null; // free memory
        }
    }

    private void checkValid() {
        int value = qcFailSample[Index.CASE] + qcFailSample[Index.CTRL];

        isValid = GenotypeLevelFilterCommand.isMaxQcFailSampleValid(value);
    }

    // initialize genotype & dpBin array for better compute performance use
    private void initGenoCovArray() {
        for (int s = 0; s < SampleManager.getListSize(); s++) {
            Sample sample = SampleManager.getList().get(s);

            Carrier carrier = carrierMap.get(sample.getId());
            NonCarrier noncarrier = noncarrierMap.get(sample.getId());

            if (carrier != null) {
                setGenoDPBin(carrier.getGenotype(), carrier.getDPBin(), s);

                if (carrier.getGenotype() == Data.INTEGER_NA) {
                    // have to remove it for init Non-carrier map
                    qcFailSample[(int) sample.getPheno()]++;
                    carrierMap.remove(sample.getId());
                }
            } else if (noncarrier != null) {
                setGenoDPBin(noncarrier.getGenotype(), noncarrier.getDPBin(), s);
            } else {
                setGenoDPBin(Data.INTEGER_NA, Data.INTEGER_NA, s);
            }
        }
    }

    private void setGenoDPBin(int geno, int bin, int s) {
        gt[s] = geno;
        dpBin[s] = bin;
    }

    public int getDPBin(int index) {
        if (index == Data.INTEGER_NA) {
            return Data.INTEGER_NA;
        }

        return dpBin[index];
    }

    public int getGT(int index) {
        if (index == Data.INTEGER_NA) {
            return Data.INTEGER_NA;
        }

        return gt[index];
    }

    public Carrier getCarrier(int sampleId) {
        return carrierMap.get(sampleId);
    }

    public int getQcFailSample(int pheno) {
        return qcFailSample[pheno];
    }
}
