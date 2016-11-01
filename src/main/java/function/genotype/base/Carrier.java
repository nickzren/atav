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

    private int dp;
    private int adRef;
    private int adAlt;
    private float gq;
    private float fs;
    private float mq;
    private float qd;
    private float qual;
    private float readPosRankSum;
    private float mqRankSum;
    private String passFailStatus;

    public Carrier(ResultSet rs) throws Exception {
        sampleId = rs.getInt("sample_id");
        gt = rs.getInt("GT");
        dp = rs.getInt("DP");
        adRef = rs.getInt("AD_REF");
        adAlt = rs.getInt("AD_ALT");
        gq = FormatManager.getFloat(rs, "GQ");
        fs = FormatManager.getFloat(rs, "FS");
        mq = FormatManager.getFloat(rs, "MQ");
        qd = FormatManager.getFloat(rs, "QD");
        qual = FormatManager.getFloat(rs, "QUAL");
        readPosRankSum = FormatManager.getFloat(rs, "ReadPosRankSum");
        mqRankSum = FormatManager.getFloat(rs, "MQRankSum");
        passFailStatus = rs.getString("FILTER");
    }

    public int getDP() {
        return dp;
    }

    public int getADRef() {
        return adRef;
    }

    public int getAdAlt() {
        return adAlt;
    }

    public float getGQ() {
        return gq;
    }

    public float getFS() {
        return fs;
    }

    public float getMQ() {
        return mq;
    }

    public float getQD() {
        return qd;
    }

    public float getQual() {
        return qual;
    }

    public float getReadPosRankSum() {
        return readPosRankSum;
    }

    public float getMQRankSum() {
        return mqRankSum;
    }

    public String getPassFailStatus() {
        return passFailStatus;
    }

    public void applyQualityFilter() {
        if (gt != Data.NA) {
            if (!GenotypeLevelFilterCommand.isVarStatusValid(passFailStatus)
                    || !GenotypeLevelFilterCommand.isGqValid(gq)
                    || !GenotypeLevelFilterCommand.isFsValid(fs)
                    || !GenotypeLevelFilterCommand.isMqValid(mq)
                    || !GenotypeLevelFilterCommand.isQdValid(qd)
                    || !GenotypeLevelFilterCommand.isQualValid(qual)
                    || !GenotypeLevelFilterCommand.isRprsValid(readPosRankSum)
                    || !GenotypeLevelFilterCommand.isMqrsValid(mqRankSum)) {
                gt = Data.NA;
            }
        }

        if (gt == Index.HOM) { // --hom-percent-alt-read 
            double percAltRead = MathManager.devide(adAlt, dp);

            if (!GenotypeLevelFilterCommand.isHomPercentAltReadValid(percAltRead)) {
                gt = Data.NA;
            }
        }

        if (gt == Index.HET) { // --het-percent-alt-read 
            double percAltRead = MathManager.devide(adAlt, dp);

            if (!GenotypeLevelFilterCommand.isHetPercentAltReadValid(percAltRead)) {
                gt = Data.NA;
            }
        }

        if (gt == Data.NA) {
            dpBin = Data.NA;
        }
    }
}
