package function.genotype.base;

import function.annotation.base.AnnotatedVariant;
import function.genotype.family.FamilyManager;
import global.Data;
import function.genotype.family.FamilyCommand;
import global.Index;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author nick
 */
public class CalledVariant extends AnnotatedVariant {

    private HashMap<Integer, Carrier> carrierMap = new HashMap<Integer, Carrier>();
    private HashMap<Integer, NonCarrier> noncarrierMap = new HashMap<Integer, NonCarrier>();
    private int[] genotype = new int[SampleManager.getListSize()];
    private int[] coverage = new int[SampleManager.getListSize()];
    private HashSet<String> familyIdSet = new HashSet<String>();

    private int[] qcFailSample = new int[2];

    public CalledVariant(int variantId, boolean isIndel, ResultSet rset) throws Exception {
        super(variantId, isIndel, rset);

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

            addFamilyIdSet(sample.getId());
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

    public int getGatkFilteredCoverage(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getGatkFilteredCoverage();
        } else {
            return Data.NA;
        }
    }

    public int getReadsRef(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getReadsRef();
        } else {
            return Data.NA;
        }
    }

    public int getReadsAlt(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getReadsAlt();
        } else {
            return Data.NA;
        }
    }

    public float getVqslod(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getVqslod();
        } else {
            return Data.NA;
        }
    }

    public float getGenotypeQualGQ(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getGenotypeQualGQ();
        } else {
            return Data.NA;
        }
    }

    public float getStrandBiasFS(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getStrandBiasFS();
        } else {
            return Data.NA;
        }
    }

    public float getHaplotypeScore(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getHaplotypeScore();
        } else {
            return Data.NA;
        }
    }

    public float getRmsMapQualMQ(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getRmsMapQualMQ();
        } else {
            return Data.NA;
        }
    }

    public float getQualByDepthQD(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getQualByDepthQD();
        } else {
            return Data.NA;
        }
    }

    public float getQual(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getQual();
        } else {
            return Data.NA;
        }
    }

    public float getReadPosRankSum(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getReadPosRankSum();
        } else {
            return Data.NA;
        }
    }

    public float getMapQualRankSum(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getMapQualRankSum();
        } else {
            return Data.NA;
        }
    }

    public String getPassFailStatus(int sampleId) {
        if (carrierMap.containsKey(sampleId)) {
            return carrierMap.get(sampleId).getPassFailStatus();
        } else {
            return "NA";
        }
    }

    private void addFamilyIdSet(int sampleId) {
        if (FamilyCommand.isFamilyAnalysis) {
            String familyId = SampleManager.getTable().get(sampleId).getFamilyId();
            if (FamilyManager.isFamilyQualified(familyId)) {
                familyIdSet.add(familyId);
            }
        }
    }

    /*
     * return qualified family id set, just for family analysis function
     */
    public HashSet<String> getFamilyIdSet() {
        return familyIdSet;
    }

    public int getQcFailSample(int pheno) {
        return qcFailSample[pheno];
    }
}
