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
    Region region;

    public Exon(int id, String stableId, String chr,
            int start, int end) {
        exon_id = id;
        stable_id = stableId;

        region = new Region(chr, start, end);
    }

    public int getExonId() {
        return exon_id;
    }

    public String getStableId() {
        return stable_id;
    }

    public Region getRegion() {
        return region;
    }

    public HashMap<Integer, Integer> getCoverage(int min_cov) {
        int [] mincovs = {min_cov};
        return CoverageManager.getCoverage(mincovs, region).get(0);
    }
    
    public boolean contains(Region region) {
        return this.region.contains(region);
    }

    @Override
    public String toString() {
        return exon_id + "_" + super.toString();
    }
}
