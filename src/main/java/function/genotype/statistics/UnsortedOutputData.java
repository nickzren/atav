package function.genotype.statistics;

/**
 *
 * @author nick
 */
public class UnsortedOutputData implements Comparable {

    double pValue;
    String line;

    public UnsortedOutputData(StatisticOutput output, double pValue) {
        this.pValue = pValue;
        line = output.toString();
    }

    @Override
    public int compareTo(Object another) throws ClassCastException {
        UnsortedOutputData that = (UnsortedOutputData) another;
        return Double.compare(this.pValue, that.pValue); //small -> large
    }
}
