package function.coverage.comparison;

import global.Data;

/**
 *
 * @author nick
 */
public class SortedSite implements Comparable {

    private String chr;
    private int pos;
    private float caseAvg;
    private float ctrlAvg;
    private float covDiff;

    public SortedSite(String chr, int pos, float caseAvg, float ctrlAvg, float covDiff) {
        this.chr = chr;
        this.pos = pos;
        this.covDiff = covDiff;
        this.caseAvg = caseAvg;
        this.ctrlAvg = ctrlAvg;
    }
    
    public String getChr(){
        return chr;
    }

    public int getPos() {
        return pos;
    }

    public float getCaseAvg() {
        return caseAvg == Data.FLOAT_NA ? 0 : caseAvg;
    }

    public float getCtrlAvg() {
        return ctrlAvg == Data.FLOAT_NA ? 0 : ctrlAvg;
    }

    public float getCovDiff() {
        return covDiff == Data.FLOAT_NA ? 0 : covDiff;
    }

    public double getCutoff(){
        return covDiff == Data.FLOAT_NA ? 0 : covDiff;
    }
    
    @Override
    public int compareTo(Object other) {
        SortedSite that = (SortedSite) other;
        return Float.compare(that.covDiff, this.covDiff); // large -> small
    }
}