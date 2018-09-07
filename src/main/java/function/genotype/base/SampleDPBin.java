package function.genotype.base;

import global.Data;

/**
 *
 * @author nick
 */
public class SampleDPBin {

    private int sampleId;
    private int dpBinCursor; // point to current coverage bin
    private int endPos; // end position of one coverage bin
    private int dpBinPos;
    private String dpBinStr;
    private short dpBin;

    public SampleDPBin(int sampleId, String covBinStr) {
        this.sampleId = sampleId;
        dpBinCursor = 0;
        endPos = 0;
        this.dpBinStr = covBinStr;
        dpBin = Data.SHORT_NA;
    }

    public int getSampleId() {
        return sampleId;
    }

    public short getDPBin(int varPosIndex) {
        if (dpBinStr != null) {
            if (endPos != 0) {
                if (varPosIndex <= endPos) {
                    return dpBin;
                } else {
                    dpBinCursor = dpBinPos + 1; // move cursor for new variant
                }
            }

            StringBuilder sb = new StringBuilder();

            for (int pos = dpBinCursor; pos < dpBinStr.length(); pos++) {
                char bin = dpBinStr.charAt(pos);
                if (!DPBinBlockManager.getCoverageBin().containsKey(bin)) {
                    sb.append(bin);
                } else {
                    dpBinPos = pos;
                    endPos += Integer.parseInt(sb.toString(), 36); // add cov bin inteval
                    sb.setLength(0); // clear StringBuilder

                    if (varPosIndex <= endPos) {
                        dpBin = DPBinBlockManager.getCoverageByBin(bin);
                        return dpBin;
                    } else {
                        dpBinCursor = dpBinPos + 1; // move cursor for current variant
                    }
                }
            }
        }

        return Data.SHORT_NA;
    }
}
