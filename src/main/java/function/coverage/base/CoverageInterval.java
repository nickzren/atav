package function.coverage.base;

import function.genotype.base.DPBinBlockManager;

/**
 *
 * @author nick
 */
public class CoverageInterval {

    private int startPos;
    private int endPos;
    private byte dpBinIndex;

    public CoverageInterval(int blockId, byte index, int startIndex, int endIndex) {
        int blockStartPos = blockId * DPBinBlockManager.DP_BIN_BLOCK_SIZE;

        startPos = blockStartPos + startIndex;
        endPos = blockStartPos + endIndex;
        
        dpBinIndex = index;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }
    
    public short getDpBinIndex() {
        return dpBinIndex;
    }
}
