package function.cohort.base;

import function.cohort.trio.TrioCommand;
import function.variant.base.Region;
import function.variant.base.VariantLevelFilterCommand;
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
    private float vqslod;
    private float sor;
    private float fs;
    private byte mq;
    private byte qd;
    private int qual;
    private float readPosRankSum;
    private float mqRankSum;
    private byte filterValue; // PASS(1), LIKELY(2), INTERMEDIATE(3), FAIL(4)
    private int pidVariantId;
    private int hpVariantId;

    public Carrier(ResultSet rs) throws Exception {
        sampleId = rs.getInt("sample_id");
        gt = rs.getByte("GT");
        dp = rs.getShort("DP");
        dpBin = Data.SHORT_NA;
        adRef = rs.getShort("AD_REF");
        adAlt = rs.getShort("AD_ALT");
        gq = FormatManager.getByte(rs, "GQ");
        vqslod = FormatManager.getFloat(rs, "VQSLOD");
        sor = FormatManager.getFloat(rs, "SOR");
        fs = FormatManager.getFloat(rs, "FS");
        mq = FormatManager.getByte(rs, "MQ");
        qd = FormatManager.getByte(rs, "QD");
        qual = FormatManager.getInt(rs, "QUAL");
        readPosRankSum = FormatManager.getFloat(rs, "ReadPosRankSum");
        mqRankSum = FormatManager.getFloat(rs, "MQRankSum");
        filterValue = rs.getByte("FILTER+0");
        // PGT = 1 indicate variant is in phase
        if (rs.getByte("PGT") == 1) {
            pidVariantId = FormatManager.getInt(rs, "PID_variant_id");
        } else {
            pidVariantId = Data.INTEGER_NA;
        }
        // HP_GT = 1 indicate variant is in phase
        if (rs.getByte("HP_GT") == 1) {
            hpVariantId = FormatManager.getInt(rs, "HP_variant_id");
        } else {
            hpVariantId = Data.INTEGER_NA;
        }
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

    public float getVQSLOD() {
        return vqslod;
    }

    public float getSOR() {
        return sor;
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
            return MathManager.getBinomialLessThan(adAlt + adRef, adAlt, 0.5f);
        }
    }

    public int getPIDVariantId() {
        return pidVariantId;
    }

    public int getHPVariantId() {
        return hpVariantId;
    }

    public void applyQualityFilter(boolean isSnv) {
        if (gt != Data.BYTE_NA) {
            if (!GenotypeLevelFilterCommand.isFilterValid(filterValue)
                    || !GenotypeLevelFilterCommand.isMinAdAltValid(adAlt)
                    || !GenotypeLevelFilterCommand.isMinGqValid(gq)
                    || !GenotypeLevelFilterCommand.isMaxSorValid(sor, isSnv)
                    || !GenotypeLevelFilterCommand.isMaxFsValid(fs, isSnv)
                    || !GenotypeLevelFilterCommand.isMinMqValid(mq)
                    || !GenotypeLevelFilterCommand.isMinQdValid(qd)
                    || !GenotypeLevelFilterCommand.isMinQualValid(qual)
                    || !GenotypeLevelFilterCommand.isMinRprsValid(readPosRankSum)
                    || !GenotypeLevelFilterCommand.isMinMqrsValid(mqRankSum)) {
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

        if (gt != Data.BYTE_NA) {
            double percentAltReadBinomialP = getPercentAltReadBinomialP();
            if (!GenotypeLevelFilterCommand.isMinPercentAltReadBinomialPValid(percentAltReadBinomialP)) {
                gt = Data.BYTE_NA;
            }

            if (!GenotypeLevelFilterCommand.isMaxPercentAltReadBinomialPValid(percentAltReadBinomialP)) {
                gt = Data.BYTE_NA;
            }
        }
    }

    /*
     * Outside of pseudoautosomal regions:
     *
     * ChrX: females are treated normally
     *
     * ChrX: males have het removed
     *
     * ChrY: females are set to missing
     *
     * ChrY: males have het removed
     *
     * Inside of pseudoautosomal region which are treated like autosomes.
     */
    protected void checkValidOnXY(Region r) {
        if (gt != Data.BYTE_NA
                && !TrioCommand.isListTrio
                && !VariantLevelFilterCommand.disableCheckOnSexChr) {
            boolean isValid = true;

            if (SampleManager.getMap().get(sampleId).isMale()) {
                if (gt == Index.HET // male het chr x or y & outside PARs
                        && !r.isInsideAutosomalOrPseudoautosomalRegions()) {
                    isValid = false;
                }
            } else if (r.getChrNum() == 24 // female chy & outside PARs
                    && !r.isInsideYPseudoautosomalRegions()) {
                isValid = false;
            }

            if (!isValid) {
                gt = Data.BYTE_NA;
            }
        }
    }
}
