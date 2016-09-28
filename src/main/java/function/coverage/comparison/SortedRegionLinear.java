package function.coverage.comparison;

/**
 *
 * @author quanli, nick
 */
public class SortedRegionLinear extends SortedRegion {

    private double pValue;
    private double r2;
    private double variance;

    public SortedRegionLinear(String name, float caseAvg, float ctrlAvg, float covDiff, int regionSize,
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
        SortedRegionLinear that = (SortedRegionLinear) other;
        return Double.compare(that.variance, this.variance); // large -> small
    }
}
