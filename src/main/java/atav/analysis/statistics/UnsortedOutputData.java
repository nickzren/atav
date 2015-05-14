package atav.analysis.statistics;

/**
 *
 * @author nick
 */
public class UnsortedOutputData implements Comparable {

    double pValue;
    String line;

    public UnsortedOutputData(StatisticOutput output) {
        pValue = output.pValue;
        line = output.toString();
    }

    public int compareTo(Object another) throws ClassCastException {
        UnsortedOutputData that = (UnsortedOutputData) another;
        return Double.compare(this.pValue, that.pValue); //small -> large
    }
}
