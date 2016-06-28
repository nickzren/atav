package function.coverage.comparison;

/**
 *
 * @author quanli, nick
 */
public class SortedRegion implements Comparable {

    private String name;
    private double caseAvg;
    private double ctrlAvg;
    private double covDiff;
    private int length;

    public SortedRegion(String name, double caseAvg, double ctrlAvg, double covDiff, int regionSize) {
        this.name = name;
        this.covDiff = covDiff;
        this.caseAvg = caseAvg;
        this.ctrlAvg = ctrlAvg;
        this.length = regionSize;
    }

    public String getName() {
        return name;
    }

    public double getCaseAvg() {
        return caseAvg;
    }

    public double getCtrlAvg() {
        return ctrlAvg;
    }

    public double getCovDiff() {
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
        return Double.compare(that.covDiff, this.covDiff); // large -> small
    }
}
