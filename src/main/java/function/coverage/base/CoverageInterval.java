package function.coverage.base;

import function.genotype.base.DPBinBlockManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;

/**
 *
 * @author nick
 */
public class CoverageInterval {

    private int startPos;
    private int endPos;

    public CoverageInterval(int blockId, int startIndex, int endIndex) {
        int blockStartPos;

        if (GenotypeLevelFilterCommand.minGQBin != Data.NO_FILTER) {
            blockStartPos = blockId * DPBinBlockManager.GQ_BIN_BLOCK_SIZE;
        } else {
            blockStartPos = blockId * DPBinBlockManager.DP_BIN_BLOCK_SIZE;
        }

        startPos = blockStartPos + startIndex;
        endPos = blockStartPos + endIndex;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }
}
