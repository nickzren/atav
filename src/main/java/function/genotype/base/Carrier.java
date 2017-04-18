package function.genotype.base;

import global.Data;
import global.Index;
import java.sql.ResultSet;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class Carrier extends NonCarrier {

    private short dp;
    private short adRef;
    private short adAlt;
    private byte gq;
    private float fs;
    private byte mq;
    private byte qd;
    private int qual;
    private float readPosRankSum;
    private float mqRankSum;
    private byte filterValue; // PASS(1), LIKELY(2), INTERMEDIATE(3), FAIL(4)

    public Carrier(ResultSet rs) throws Exception {
        sampleId = rs.getInt("sample_id");
        gt = rs.getByte("GT");
        dp = rs.getShort("DP");
        dpBin = Data.SHORT_NA;
        adRef = rs.getShort("AD_REF");
        adAlt = rs.getShort("AD_ALT");
        gq = FormatManager.getByte(rs, "GQ");
        fs = FormatManager.getFloat(rs, "FS");
        mq = FormatManager.getByte(rs, "MQ");
        qd = FormatManager.getByte(rs, "QD");
        qual = FormatManager.getInt(rs, "QUAL");
        readPosRankSum = FormatManager.getFloat(rs, "ReadPosRankSum");
        mqRankSum = FormatManager.getFloat(rs, "MQRankSum");
        filterValue = rs.getByte("FILTER+0");
    }

    public short getDP() {
        return dp;
    }

    public short getADRef() {
        return adRef;
    }

    public short getADAlt() {
        return adAlt;
    }

    public byte getGQ() {
        return gq;
    }

    public float getFS() {
        return fs;
    }

    public byte getMQ() {
        return mq;
    }

    public byte getQD() {
        return qd;
    }

    public int getQual() {
        return qual;
    }

    public float getReadPosRankSum() {
        return readPosRankSum;
    }

    public float getMQRankSum() {
        return mqRankSum;
    }

    public String getFILTER() {
        return GenotypeLevelFilterCommand.FILTER[filterValue - 1];
    }

    public String getPercAltRead() {
        return FormatManager.getFloat(MathManager.devide(adAlt, dp));
    }

    public double getPercentAltReadBinomialP() {
        if (adAlt == Data.SHORT_NA || adRef == Data.SHORT_NA) {
            return Data.DOUBLE_NA;
        } else {
            return MathManager.getBinomial(adAlt + adRef, adAlt, 0.5f);
        }
    }

    public void applyQualityFilter(boolean isSnv) {
        if (gt != Data.BYTE_NA) {
            if (!GenotypeLevelFilterCommand.isFilterValid(filterValue)
                    || !GenotypeLevelFilterCommand.isGqValid(gq, isSnv)
                    || !GenotypeLevelFilterCommand.isFsValid(fs, isSnv)
                    || !GenotypeLevelFilterCommand.isMqValid(mq, isSnv)
                    || !GenotypeLevelFilterCommand.isQdValid(qd, isSnv)
                    || !GenotypeLevelFilterCommand.isQualValid(qual, isSnv)
                    || !GenotypeLevelFilterCommand.isRprsValid(readPosRankSum, isSnv)
                    || !GenotypeLevelFilterCommand.isMqrsValid(mqRankSum, isSnv)) {
                gt = Data.BYTE_NA;
            }
        }

        if (gt == Index.HOM) { // --hom-percent-alt-read 
            float percAltRead = MathManager.devide(adAlt, dp);

            if (!GenotypeLevelFilterCommand.isHomPercentAltReadValid(percAltRead)) {
                gt = Data.BYTE_NA;
            }
        }

        if (gt == Index.HET) { // --het-percent-alt-read 
            float percAltRead = MathManager.devide(adAlt, dp);

            if (!GenotypeLevelFilterCommand.isHetPercentAltReadValid(percAltRead)) {
                gt = Data.BYTE_NA;
            }
        }

        if (gt == Data.BYTE_NA) {
            dpBin = Data.SHORT_NA;
        }
    }
}
