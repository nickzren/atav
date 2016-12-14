package function.genotype.base;

import function.annotation.base.AnnotatedVariant;
import function.variant.base.VariantManager;
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
    private byte[] gt = new byte[SampleManager.getListSize()];
    private short[] dpBin = new short[SampleManager.getListSize()];
    private int[] qcFailSample = new int[2];

    public CalledVariant(String chr, int variantId, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        init();
    }

    private void init() throws Exception {
        if (isValid) {
            if (VariantManager.isUsed()) { // when --variant or --rs-number applied
                // single variant carriers data process
                CarrierBlockManager.initCarrierMap(carrierMap, this);

                if (carrierMap.isEmpty()) {
                    isValid = false;
                    return;
                }
            } else {
                // block variants carriers data process
                CarrierBlockManager.init(this);

                carrierMap = CarrierBlockManager.getVarCarrierMap(variantId);

                if (carrierMap == null) {
                    isValid = false;
                    return;
                }
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
                setGenoDPBin(carrier.getGT(), carrier.getDPBin(), s);

                if (carrier.getGT() == Data.BYTE_NA) {
                    // have to remove it for init Non-carrier map
                    qcFailSample[sample.getPheno()]++;
                    carrierMap.remove(sample.getId());
                }
            } else if (noncarrier != null) {
                setGenoDPBin(noncarrier.getGT(), noncarrier.getDPBin(), s);
            } else {
                setGenoDPBin(Data.BYTE_NA, Data.SHORT_NA, s);
            }
        }
    }

    private void setGenoDPBin(byte geno, short bin, int s) {
        gt[s] = geno;
        dpBin[s] = bin;
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

    public int getQcFailSample(int pheno) {
        return qcFailSample[pheno];
    }
}
