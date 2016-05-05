package function.coverage.base;

import function.variant.base.Region;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author qwang
 */
public class Exon {

    int exon_id;
    String stable_id;
    String transcript_stable_id;
    CoveredRegion covRegion;

    public Exon(int id, String stableId, int region_id, String chr,
            int start, int end, String trans_stable_id) {
        exon_id = id;
        stable_id = stableId;
        transcript_stable_id = trans_stable_id;

        covRegion = new CoveredRegion(region_id, chr, start, end);
    }

    public int getExonId() {
        return exon_id;
    }

    public String getStableId() {
        return stable_id;
    }

    public String getTransStableId() {
        return transcript_stable_id;
    }

    public void setRegion(CoveredRegion r) {
        covRegion = r;
    }

    public CoveredRegion getCoveredRegion() {
        return covRegion;
    }

    public HashMap<Integer, Integer> getCoverage(int min_cov) {
        int [] mincovs = {min_cov};
        return covRegion.getCoverage(mincovs).get(0);
    }
    
    public ArrayList<HashMap<Integer, Integer>> getCoverage(int[] min_cov) {
        return covRegion.getCoverage(min_cov);
    }
    
    public boolean contains(Region region) {
        return covRegion.contains(region);
    }

    @Override
    public String toString() {
        return exon_id + "_" + super.toString();
    }
}
