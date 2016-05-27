package function.coverage.base;

import function.genotype.base.CoverageBlockManager;

/**
 *
 * @author nick
 */
public class CoverageInterval {

    private int startPos;
    private int endPos;
    private int coverage;

    public CoverageInterval(int sampleBlockPos, int startIndex, int endIndex, int cov) {
        int sampleStartPos = sampleBlockPos - CoverageBlockManager.COVERAGE_BLOCK_SIZE;

        startPos = sampleStartPos + startIndex;
        endPos = sampleStartPos + endIndex;
        coverage = cov;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public int getCoverage() {
        return coverage;
    }
}
