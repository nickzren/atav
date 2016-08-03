package function.genotype.base;

import function.variant.base.Variant;
import global.Data;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author nick, qwang
 */
public class CoverageBlockManager {

    public static final int COVERAGE_BLOCK_SIZE = 1024;

    private static ArrayList<SampleCoverageBin> currentBlockList = new ArrayList<>();
    private static int currentBlockEndPos = Data.NA;

    private static HashMap<Character, Integer> coverageBin = new HashMap<>();

    public static void init() {
        coverageBin.put('a', Data.NA);
        coverageBin.put('b', 3);
        coverageBin.put('c', 10);
        coverageBin.put('d', 20);
        coverageBin.put('e', 201);
    }
    
    public static void add(SampleCoverageBin covBin) {
        currentBlockList.add(covBin);
    }

    public static void initNonCarrierMap_test(Variant var,
            HashMap<Integer, Carrier> carrierMap,
            HashMap<Integer, NonCarrier> noncarrierMap) {
        int varPosIndex = getVarPosIndex(var);
        int blockEndPos = getBlockEndPos(var, varPosIndex);

        if (blockEndPos == currentBlockEndPos
                && currentBlockEndPos != Data.NA) {
            for (SampleCoverageBin covBin : currentBlockList) {
                if (!carrierMap.containsKey(covBin.getSampleId())) {
                    NonCarrier noncarrier = new NonCarrier(covBin.getSampleId(), covBin.getCoverage(varPosIndex));

                    noncarrier.applyFilters(var);

                    if (noncarrier.isValid()) {
                        noncarrierMap.put(noncarrier.getSampleId(), noncarrier);
                    }
                }
            }
        } else {
            currentBlockEndPos = blockEndPos;
            currentBlockList.clear();

            SampleManager.initNonCarrierMap(var, carrierMap, noncarrierMap);
        }
    }

    public static int getCoverageByBin(Character bin) {
        return coverageBin.get(bin);
    }

    public static HashMap<Character, Integer> getCoverageBin() {
        return coverageBin;
    }

    protected static int getVarPosIndex(Variant var) {
        int posIndex = var.getStartPosition() % COVERAGE_BLOCK_SIZE;

        if (posIndex == 0) {
            posIndex = COVERAGE_BLOCK_SIZE; // block boundary is ( ] 
        }

        return posIndex;
    }

    protected static int getBlockEndPos(Variant var, int varPosIndex) {
        return var.getStartPosition() - varPosIndex + COVERAGE_BLOCK_SIZE;
    }
}
