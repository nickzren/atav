package function.coverage.base;

import function.genotype.base.DPBinBlockManager;

/**
 *
 * @author nick
 */
public class CoverageInterval {

    private int startPos;
    private int endPos;

    public CoverageInterval(int sampleBlockPos, int startIndex, int endIndex) {
        int sampleStartPos = sampleBlockPos - DPBinBlockManager.DP_BIN_BLOCK_SIZE;

        startPos = sampleStartPos + startIndex;
        endPos = sampleStartPos + endIndex;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }
}
