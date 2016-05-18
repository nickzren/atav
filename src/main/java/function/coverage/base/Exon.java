package function.coverage.base;

import function.variant.base.Region;
import java.util.HashMap;

/**
 *
 * @author qwang
 */
public class Exon {

    int exon_id;
    String stable_id;
    CoveredRegion covRegion;

    public Exon(int id, String stableId, int region_id, String chr,
            int start, int end) {
        exon_id = id;
        stable_id = stableId;

        covRegion = new CoveredRegion(region_id, chr, start, end);
    }

    public int getExonId() {
        return exon_id;
    }

    public String getStableId() {
        return stable_id;
    }

    public CoveredRegion getCoveredRegion() {
        return covRegion;
    }

    public HashMap<Integer, Integer> getCoverage(int min_cov) {
        int [] mincovs = {min_cov};
        return covRegion.getCoverage(mincovs).get(0);
    }
    
    public boolean contains(Region region) {
        return covRegion.contains(region);
    }

    @Override
    public String toString() {
        return exon_id + "_" + super.toString();
    }
}
