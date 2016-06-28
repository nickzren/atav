package function.genotype.base;

import function.variant.base.Variant;
import global.Data;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author qwang, nick
 */
public class CoverageBlockManager {

    public static final int COVERAGE_BLOCK_SIZE = 1024;

    private static HashMap<Integer, int[][]> currentBlockMap = new HashMap<Integer, int[][]>();
    private static int currentBlockEndPos = Data.NA;

    private static HashMap<Character, Integer> coverageBin = new HashMap<Character, Integer>();

    public static void init() {
        coverageBin.put('a', Data.NA);
        coverageBin.put('b', 3);
        coverageBin.put('c', 10);
        coverageBin.put('d', 20);
        coverageBin.put('e', 201);
    }

    public static void put(int sampleId, String coverageStr) {
        currentBlockMap.put(sampleId, parseCoverage(coverageStr));
    }

    public static void initNonCarrierMap(Variant var,
            HashMap<Integer, Carrier> carrierMap,
            HashMap<Integer, NonCarrier> noncarrierMap) {
        int varPosIndex = getVarPosIndex(var);
        int blockEndPos = getBlockEndPos(var, varPosIndex);

        if (blockEndPos == currentBlockEndPos
                && currentBlockEndPos != Data.NA) {
            for (Entry<Integer, int[][]> entry : currentBlockMap.entrySet()) { // sampleId, allCovBin                
                if (!carrierMap.containsKey(entry.getKey())) {
                    NonCarrier noncarrier = new NonCarrier(entry.getKey(), getCoverage(varPosIndex, entry.getValue()));
                    
                    noncarrier.applyFilters(var);
                    
                    if (noncarrier.isValid()) {
                        noncarrierMap.put(noncarrier.getSampleId(), noncarrier);
                    }
                }
            }
        } else {
            currentBlockEndPos = blockEndPos;
            currentBlockMap.clear();

            SampleManager.initNonCarrierMap(var, carrierMap, noncarrierMap);
        }
    }

    private static int getCoverage(int posIndex, int[][] allCovBin) {
        for (int i = 0; i < allCovBin.length; i++) {
            if (posIndex <= allCovBin[i][0]) {
                return allCovBin[i][1];
            }
        }

        return Data.NA;
    }

    private static int[][] parseCoverage(String allCov) {
        String[] allCovArray = allCov.split(",");
        int[][] allCovBin = new int[allCovArray.length][2];
        int covBinPos = 0;

        for (int i = 0; i < allCovArray.length; i++) {
            int covBinLength = allCovArray[i].length();
            covBinPos += Integer.valueOf(allCovArray[i].substring(0, covBinLength - 1));
            allCovBin[i][0] = covBinPos;
            allCovBin[i][1] = getCoverageByBin(allCovArray[i].charAt(covBinLength - 1));
        }

        return allCovBin;
    }

    public static int getCoverageByBin(Character bin) {
        return coverageBin.get(bin);
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
