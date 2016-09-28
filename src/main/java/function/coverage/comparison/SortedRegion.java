package function.coverage.comparison;

/**
 *
 * @author quanli, nick
 */
public class SortedRegion implements Comparable {

    private String name;
    private float caseAvg;
    private float ctrlAvg;
    private float covDiff;
    private int length;

    public SortedRegion(String name, float caseAvg, float ctrlAvg, float covDiff, int regionSize) {
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

    public int getLength() {
        return length;
    }

    public double getCutoff(){
        return covDiff;
    }
    
    @Override
    public int compareTo(Object other) {
        SortedRegion that = (SortedRegion) other;
        return Float.compare(that.covDiff, this.covDiff); // large -> small
    }
}
