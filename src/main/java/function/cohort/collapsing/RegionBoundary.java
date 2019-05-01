package function.cohort.collapsing;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nick
 */
public class RegionBoundary {

    private String name;
    private List<Region> regionList = new ArrayList<Region>();

    public RegionBoundary(String regionBoundaryStr) {
        String[] tmp = regionBoundaryStr.split("\\s+");

        name = tmp[0];
        
        initRegionList(tmp[1]);
    }

    public String getName() {
        return name;
    }

    public List<Region> getList() {
        return regionList;
    }

    private void initRegionList(String boundaryStr) {
        for (String regionStr : boundaryStr.split(",")) {
            regionList.add(new Region(regionStr));
        }
    }

    public boolean isContained(String chr, int pos) {
        for (Region region : regionList) {
            if (region.isContained(chr, pos)) {
                return true;
            }
        }

        return false;
    }

    class Region {

        String chr;
        public int start;
        public int end;

        public Region(String regionStr) { // chr:start-end
            String[] tmp = regionStr.split(":|-");

            chr = tmp[0];
            start = Integer.valueOf(tmp[1]);
            end = Integer.valueOf(tmp[2]);
        }

        public boolean isContained(String chr, int pos) {
            if (this.chr.equalsIgnoreCase(chr)
                    && pos >= this.start
                    && pos <= this.end) {
                return true;
            }

            return false;
        }

        public String toString() {
            return chr + ":" + start + "-" + end;
        }
    }
}
