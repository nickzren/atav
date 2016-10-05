package function.coverage.comparison;

/**
 *
 * @author quanli, nick
 */
public class SortedExon implements Comparable {

    private String name;
    private float caseAvg;
    private float ctrlAvg;
    private float covDiff;
    private int length;

    public SortedExon(String name, float caseAvg, float ctrlAvg, float covDiff, int regionSize) {
        this.name = name;
        this.covDiff = covDiff;
        this.caseAvg = caseAvg;
        this.ctrlAvg = ctrlAvg;
        this.length = regionSize;
    }

    public String getName() {
        return name;
    }

    public float getCaseAvg() {
        return caseAvg;
    }

    public float getCtrlAvg() {
        return ctrlAvg;
    }

    public float getCovDiff() {
        return covDiff;
    }

    public double getCutoff() {
        return covDiff;
    }

    public int getLength() {
        return length;
    }

    @Override
    public int compareTo(Object other) {
        SortedExon that = (SortedExon) other;
        return Float.compare(that.covDiff, this.covDiff); // large -> small
    }
}
