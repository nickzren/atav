package function.genotype.base;

import global.Data;

/**
 *
 * @author nick
 */
public class SampleCoverageBin {

    private int sampleId;
    private int covBinCursor; // point to current coverage bin
    private int endPos; // end position of one coverage bin
    private int covBinPos;
    private String covBinStr;
    private int covBin;

    public SampleCoverageBin(int sampleId, String covBinStr) {
        this.sampleId = sampleId;
        covBinCursor = 0;
        endPos = 0;
        this.covBinStr = covBinStr;
        covBin = Data.NA;
    }

    public int getSampleId() {
        return sampleId;
    }

    public int getCoverage(int varPosIndex) {
        if (endPos != 0) {
            if (varPosIndex <= endPos) {
                return covBin;
            } else {
                covBinCursor = covBinPos + 2; // move cursor for new variant
            }
        }
        
        StringBuilder sb = new StringBuilder();

        for (int pos = covBinCursor; pos < covBinStr.length(); pos++) {
            char c = covBinStr.charAt(pos);
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (CoverageBlockManager.getCoverageBin().containsKey(c)) {
                covBinPos = pos;
                endPos += Integer.parseInt(sb.toString()); // add cov bin inteval
                sb.setLength(0); // clear StringBuilder

                if (varPosIndex <= endPos) {
                    covBin = CoverageBlockManager.getCoverageByBin(c);
                    return covBin;
                } else {
                    covBinCursor = covBinPos + 2; // move cursor for current variant
                }
            }
        }

        return Data.NA;
    }
}
