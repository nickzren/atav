package function.genotype.base;

import function.annotation.base.AnnotatedVariant;
import function.annotation.base.AnnotatedVariant;
import function.genotype.base.Carrier;
import function.genotype.base.NonCarrier;
import function.genotype.base.Sample;
import function.genotype.family.FamilyManager;
import global.Data;
import function.external.evs.EvsManager;
import function.variant.base.RegionManager;
import temp.TempRange;
import utils.CommandValue;
import utils.FormatManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author nick
 */
public class CalledVariant extends AnnotatedVariant {

    private HashMap<Integer, Carrier> carrierMap = new HashMap<Integer, Carrier>();
    private HashMap<Integer, Carrier> evsEaCarrierMap = new HashMap<Integer, Carrier>();
    private HashMap<Integer, Carrier> evsAaCarrierMap = new HashMap<Integer, Carrier>();
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
            SampleManager.initCarrierMap(this, carrierMap, evsEaCarrierMap, evsAaCarrierMap);

            CoverageBlockManager.initNonCarrierMap(this, carrierMap, noncarrierMap);

            resetPhs000473SampleGeno();

            initCalledInfo();

            evsEaCarrierMap = null;
            evsAaCarrierMap = null;
            noncarrierMap = null;
        }
    }

    // temp hack solution - phs000473 coverage restriction
    private void resetPhs000473SampleGeno() {
        if (SampleManager.phs000473SampleIdSet.isEmpty()) {
            return;
        }

        ArrayList<TempRange> rangeList = RegionManager.phs000473RegionMap.get(region.chrStr);

        boolean isContained = false;

        for (TempRange range : rangeList) {
            if (region.startPosition >= range.start
                    && region.startPosition <= range.end) {
                isContained = true;
                break;
            }
        }

        if (!isContained) {
            for (int sampleId : SampleManager.phs000473SampleIdSet) {
                NonCarrier noncarrier = noncarrierMap.get(sampleId);

                if (noncarrier != null) {
                    noncarrier.genotype = Data.NA;
                    noncarrier.coverage = Data.NA;
                }
            }
        }
    }

    private void initCalledInfo() {
        int evsEaCoverage = getEvsCoverage("ea");
        int evsAaCoverage = getEvsCoverage("aa");

        int evsEaHomRefNum = getEvsHomRefNum("ea", evsEaCoverage, evsEaCarrierMap.size());
        int evsAaHomRefNum = getEvsHomRefNum("aa", evsAaCoverage, evsAaCarrierMap.size());

        int eaNum = 0, aaNum = 0;

        for (int s = 0; s < SampleManager.getListSize(); s++) {
            Sample sample = SampleManager.getList().get(s);

            if (SampleManager.isEvsEaSampleId(sample.getId(), isIndel())) {
                if (evsEaCoverage <= 0) {
                    setGenoCov(Data.NA, Data.NA, s);
                    carrierMap.remove(sample.getId());
                    continue;
                }

                eaNum = initEvsGenoCov(sample.getId(), s, eaNum, evsEaHomRefNum, evsEaCarrierMap);
            } else if (SampleManager.isEvsAaSampleId(sample.getId(), isIndel())) {
                if (evsAaCoverage <= 0) {
                    setGenoCov(Data.NA, Data.NA, s);
                    carrierMap.remove(sample.getId());
                    continue;
                }

                aaNum = initEvsGenoCov(sample.getId(), s, aaNum, evsAaHomRefNum, evsAaCarrierMap);
            } else {
                initChgvGenoCov(sample, s);

                addFamilyIdSet(sample.getId());
            }
        }
    }

    private int initEvsGenoCov(int sampleId, int s,
            int num, int evsHomRefNum,
            HashMap<Integer, Carrier> evsCarrierMap) {
        Carrier carrier = evsCarrierMap.get(sampleId);

        if (carrier != null) {
            setGenoCov(carrier.getGenotype(), carrier.getCoverage(), s);

            if (carrier.getGenotype() == Data.NA) {
                // have to remove it
                carrierMap.remove(sampleId);
            }
        } else if (num < evsHomRefNum) {
            num++;
            setGenoCov(0, 8, s);
        } else {
            setGenoCov(Data.NA, Data.NA, s);
        }

        return num;
    }

    private void setGenoCov(int geno, int cov, int s) {
        genotype[s] = geno;
        coverage[s] = cov;
    }

    public int getEvsHomRefNum(String evsSample, int evsCoverage, int evsCarrier) {
        int evsHomRefNum = 0;

        if (evsCoverage > 0) {
            evsHomRefNum = (int) (evsCoverage * FormatManager.devide(
                    SampleManager.getEvsSampleNum(evsSample, isIndel()),
                    EvsManager.getTotalEvsNum(evsSample))
                    - evsCarrier);

            if (evsHomRefNum < 0) {
                evsHomRefNum = 0;
            }
        }

        return evsHomRefNum;
    }

    private void initChgvGenoCov(Sample sample, int s) {
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
        if (CommandValue.isFamilyAnalysis) {
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
