package function.coverage.base;

import function.genotype.base.CoverageBlockManager;
import function.variant.base.Region;
import function.genotype.base.SampleManager;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author qwang
 */
public class CoveredRegion extends Region {

    public CoveredRegion(int id, String chr, int start, int end) {
        super(id, chr, start, end);
    }

    public CoveredRegion(String chr, int start, int end) {
        super(chr, start, end);
    }
    
    public CoveredRegion intersect(int start, int end) {
        if (end >= startPosition && start <= endPosition) {
            int newstart = Math.max(startPosition, start);
            int newend = Math.min(endPosition, end);
            return new CoveredRegion(regionId, chrStr, newstart, newend);
        }
        return null;
    }

    public int intersectLength(int region_start, int region_end) {
        if (region_end >= startPosition && region_start <= endPosition) {
            int start = Math.max(startPosition, region_start);
            int end = Math.min(endPosition, region_end);
            return end - start + 1;
        } else {
            return 0;
        }
    }
}
