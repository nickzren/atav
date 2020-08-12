package function.cohort.collapsing;

import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import function.cohort.statistics.FisherExact;
import global.Data;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class CollapsingSummary implements Comparable {

    String name; // gene name or region name

    byte[] hasQualifiedVariantBySample = new byte[SampleManager.getTotalSampleNum()];

    int totalVariant = 0;
    int totalSnv = 0;
    int totalIndel = 0;
    // a b c d will be passed to calculated fisher p
    int qualifiedCase = 0;  // a
    int unqualifiedCase = 0; // b
    int qualifiedCtrl = 0;   // c
    int unqualifiedCtrl = 0; // d 
    float qualifiedCaseFreq = Data.FLOAT_NA;
    float qualifiedCtrlFreq = Data.FLOAT_NA;
    String enrichedDirection = Data.STRING_NA;
    double fetP = Data.DOUBLE_NA;
    double logisticP = Data.DOUBLE_NA;
    double linearP = Data.DOUBLE_NA;

    // no output
    static final int totalCase = SampleManager.getCaseNum();
    static final int totalCtrl = SampleManager.getCtrlNum();

    public CollapsingSummary(String name) {
        this.name = name;
    }

    public void setLogisticP(double value) {
        logisticP = value;
    }

    public void setLinearP(double value) {
        linearP = value;
    }

    public void countQualifiedVariantBySample(int index) {
        if (hasQualifiedVariantBySample[index] == 0) {
            hasQualifiedVariantBySample[index] = 1;
        }
    }

    public void updateVariantCount(boolean isSnv) {
        totalVariant++;

        if (isSnv) {
            totalSnv++;
        } else {
            totalIndel++;
        }
    }

    public void countSample() {
        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()) {
                if (hasQualifiedVariantBySample[sample.getIndex()] > 0) {
                    qualifiedCase++;
                }
            } else {
                if (hasQualifiedVariantBySample[sample.getIndex()] > 0) {
                    qualifiedCtrl++;
                }
            }
        }

        unqualifiedCase = totalCase - qualifiedCase;
        unqualifiedCtrl = totalCtrl - qualifiedCtrl;

        qualifiedCaseFreq = MathManager.devide(qualifiedCase, totalCase);
        qualifiedCtrlFreq = MathManager.devide(qualifiedCtrl, totalCtrl);

        if (qualifiedCaseFreq == 0
                && qualifiedCtrlFreq == 0) {
            enrichedDirection = Data.STRING_NA;
        } else if (qualifiedCaseFreq == qualifiedCtrlFreq) {
            enrichedDirection = "none";
        } else if (qualifiedCaseFreq < qualifiedCtrlFreq) {
            enrichedDirection = "ctrl";
        } else if (qualifiedCaseFreq > qualifiedCtrlFreq) {
            enrichedDirection = "case";
        }
    }

    public void calculateFetP() {
        fetP = FisherExact.getTwoTailedP(qualifiedCase, unqualifiedCase,
                qualifiedCtrl, unqualifiedCtrl);
    }

    public double getFetP() {
        return fetP;
    }

    @Override
    public int compareTo(Object another) throws ClassCastException {
        CollapsingSummary that = (CollapsingSummary) another;
        return Double.compare(this.fetP, that.fetP); //small -> large
    }
}
