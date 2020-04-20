package function.cohort.base;

import global.Data;
import global.Index;

/**
 *
 * @author nick
 */
public class NonCarrier {

    protected int sampleId;
    protected byte gt;
    protected short dpBin;

    public NonCarrier() {
    }

    public NonCarrier(int sampleId, short dpBin) {
        this.sampleId = sampleId;
        this.dpBin = dpBin;

        initGenotype();
    }

    public NonCarrier(int sampleId, String minDPBin, int posIndex) throws Exception {
        this.sampleId = sampleId;
        SampleDPBin sampleDPBin = new SampleDPBin(sampleId, minDPBin);
        DPBinBlockManager.add(sampleDPBin);
        dpBin = sampleDPBin.getDPBin(posIndex);

        initGenotype();
    }

    private void initGenotype() {
        if (dpBin == Data.SHORT_NA) {
            gt = Data.BYTE_NA;
        } else {
            gt = Index.REF;
        }
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setGT(byte value) {
        gt = value;
    }

    public byte getGT() {
        return gt;
    }

    public void setDPBin(short value) {
        dpBin = value;
    }

    public short getDPBin() {
        return dpBin;
    }

    public void applyCoverageFilter() {
        if (!GenotypeLevelFilterCommand.isMinDpBinValid(dpBin)) {
            gt = Data.BYTE_NA;
        }
    }

    public boolean isValid() {
        return gt != Data.BYTE_NA;
    }
}
