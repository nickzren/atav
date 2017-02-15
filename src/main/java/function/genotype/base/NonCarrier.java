package function.genotype.base;

import global.Data;
import global.Index;

/**
 *
 * @author nick
 */
public class NonCarrier {

    public int sampleId;
    public byte gt;
    public short dpBin;

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

    public byte getGT() {
        return gt;
    }

    public void setDPBin(short value) {
        dpBin = value;
    }

    public short getDPBin() {
        return dpBin;
    }

    public void applyCoverageFilter(int minCaseCov, int minCtrlCov) {
        Sample sample = SampleManager.getMap().get(sampleId);

        if (sample.isCase()) // --min-case-coverage-call or --min-case-coverage-no-call
        {
            if (!GenotypeLevelFilterCommand.isMinCoverageValid(dpBin, minCaseCov)) {
                setMissing();
            }
        } else // --min-ctrl-coverage-call or --min-ctrl-coverage-no-call
         if (!GenotypeLevelFilterCommand.isMinCoverageValid(dpBin, minCtrlCov)) {
                setMissing();
            }
    }

    private void setMissing() {
        gt = Data.BYTE_NA;
        dpBin = Data.SHORT_NA;
    }

    public boolean isValid() {
        return gt != Data.BYTE_NA;
    }
}
