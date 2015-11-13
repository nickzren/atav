package function.genotype.collapsing;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import function.genotype.statistics.FisherExact;
import global.Data;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CollapsingSummary implements Comparable {

    String name; // gene name or region name

    int[] variantNumBySample = new int[SampleManager.getListSize()];

    int totalVariant = 0;
    int totalSnv = 0;
    int totalIndel = 0;
    // a b c d will be passed to calculated fisher p
    int qualifiedCase = 0;  // a
    int unqualifiedCase = 0; // b
    int qualifiedCtrl = 0;   // c
    int unqualifiedCtrl = 0; // d 
    double qualifiedCaseFreq = Data.NA;
    double qualifiedCtrlFreq = Data.NA;
    String enrichedDirection = "NA";
    double fetP = Data.NA;

    // no output
    static final int totalCase = SampleManager.getCaseNum();
    static final int totalCtrl = SampleManager.getCtrlNum();

    public CollapsingSummary(String name) {
        this.name = name;
    }

    public void updateSampleVariantCount4SingleVar(int index) {
        variantNumBySample[index] = variantNumBySample[index] + 1;
    }

    public void updateSampleVariantCount4CompHet(int index) {
        if (variantNumBySample[index] == 0) {
            variantNumBySample[index] = variantNumBySample[index] + 1;
        }
    }

    public void updateVariantCount(CollapsingOutput output) {
        totalVariant++;

        if (output.getCalledVariant().isSnv()) {
            totalSnv++;
        } else {
            totalIndel++;
        }
    }

    public void updateVariantCount(CompHetOutput output) {
        totalVariant++;

        if (output.getCalledVariant().isSnv()) {
            totalSnv++;
        } else {
            totalIndel++;
        }
    }

    public void countSample() {
        for (int s = 0; s < SampleManager.getListSize(); s++) {
            Sample sample = SampleManager.getList().get(s);
            if (sample.isCase()) {
                if (variantNumBySample[s] > 0) {
                    qualifiedCase++;
                }
            } else {
                if (variantNumBySample[s] > 0) {
                    qualifiedCtrl++;
                }
            }
        }

        unqualifiedCase = totalCase - qualifiedCase;
        unqualifiedCtrl = totalCtrl - qualifiedCtrl;

        qualifiedCaseFreq = FormatManager.devide(qualifiedCase, totalCase);
        qualifiedCtrlFreq = FormatManager.devide(qualifiedCtrl, totalCtrl);

        if (qualifiedCaseFreq == 0
                && qualifiedCtrlFreq == 0) {
            enrichedDirection = "NA";
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
        CollapsingGeneSummary that = (CollapsingGeneSummary) another;
        return Double.compare(this.fetP, that.fetP); //small -> large
    }
}
