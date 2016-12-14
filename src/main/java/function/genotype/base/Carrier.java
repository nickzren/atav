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
    private int adRef;
    private int adAlt;
    private int gq;
    private float vqslod;
    private float fs;
    private int mq;
    private int qd;
    private int qual;
    private float readPosRankSum;
    private float mqRankSum;
    private int filterValue; // PASS(1), LIKELY(2), INTERMEDIATE(3), FAIL(4)

    public Carrier(ResultSet rs) throws Exception {
        sampleId = rs.getInt("sample_id");
        gt = rs.getByte("GT");
        dp = rs.getShort("DP");
        dpBin = Data.SHORT_NA;
        adRef = rs.getInt("AD_REF");
        adAlt = rs.getInt("AD_ALT");
        gq = FormatManager.getInt(rs, "GQ");
        vqslod = FormatManager.getFloat(rs, "VQSLOD");
        fs = FormatManager.getFloat(rs, "FS");
        mq = FormatManager.getInt(rs, "MQ");
        qd = FormatManager.getInt(rs, "QD");
        qual = FormatManager.getInt(rs, "QUAL");
        readPosRankSum = FormatManager.getFloat(rs, "ReadPosRankSum");
        mqRankSum = FormatManager.getFloat(rs, "MQRankSum");
        filterValue = rs.getInt("FILTER+0");
    }

    public short getDP() {
        return dp;
    }

    public int getADRef() {
        return adRef;
    }

    public int getAdAlt() {
        return adAlt;
    }

    public int getGQ() {
        return gq;
    }

    public float getVqslod() {
        return vqslod;
    }

    public float getFS() {
        return fs;
    }

    public int getMQ() {
        return mq;
    }

    public int getQD() {
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
        return GenotypeLevelFilterCommand.VCF_FILTER[filterValue - 1];
    }

    public void applyQualityFilter() {
        if (gt != Data.BYTE_NA) {
            if (!GenotypeLevelFilterCommand.isVcfFilterValid(filterValue)
                    || !GenotypeLevelFilterCommand.isGqValid(gq)
                    || !GenotypeLevelFilterCommand.isFsValid(fs)
                    || !GenotypeLevelFilterCommand.isMqValid(mq)
                    || !GenotypeLevelFilterCommand.isQdValid(qd)
                    || !GenotypeLevelFilterCommand.isQualValid(qual)
                    || !GenotypeLevelFilterCommand.isRprsValid(readPosRankSum)
                    || !GenotypeLevelFilterCommand.isMqrsValid(mqRankSum)) {
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
