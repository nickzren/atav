package atav.analysis.coverage.base;

import atav.global.Data;

/**
 *
 * @author nick
 */
public class CoverageInterval implements Comparable {

    private int startPos;
    private int endPos;
    private int coverage;

    public CoverageInterval(int sampleBlockPos, int startIndex, int endIndex, int cov) {
        int sampleStartPos = sampleBlockPos - Data.COVERAGE_BLOCK_SIZE;

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

    public int compareTo(Object another) throws ClassCastException {
        CoverageInterval that = (CoverageInterval) another;
        return Double.compare(this.coverage, that.coverage); //small -> large
    }
}
