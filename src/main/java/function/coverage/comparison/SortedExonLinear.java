package function.coverage.comparison;

import global.Data;

/**
 *
 * @author quanli, nick
 */
public class SortedExonLinear extends SortedExon {

    private double pValue;
    private double r2;
    private double variance;

    public SortedExonLinear(String name, float caseAvg, float ctrlAvg, float covDiff, int regionSize,
            double p, double r2, double variance) {
        super(name, caseAvg, ctrlAvg, covDiff, regionSize);

        this.pValue = p;
        this.r2 = r2;
        this.variance = variance;
    }

    public double getPvalue() {
        return pValue;
    }

    public double getR2() {
        return r2;
    }

    public double getVariant() {
        return variance;
    }

    @Override
    public double getCutoff() {
        return variance;
    }

    @Override
    public int compareTo(Object other) {
        SortedExonLinear that = (SortedExonLinear) other;
        return Double.compare(that.variance, this.variance); // large -> small
    }
}
