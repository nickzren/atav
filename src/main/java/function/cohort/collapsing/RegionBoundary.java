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
        // gene/domain boundary format: name chr (start..end,start..end,start..end) length
        // region boundary format: name chr:start-end,chr:start-end,chr:start-end
        String[] tmp = regionBoundaryStr.split("\\s+");

        name = tmp[0];

        if (regionBoundaryStr.contains("(")) { // input gene/domain boundary format
            initRegionList(tmp[1], tmp[2]);
        } else {
            initRegionList(tmp[1]);
        }
    }

    public String getName() {
        return name;
    }

    public List<Region> getList() {
        return regionList;
    }

    private void initRegionList(String chr, String boundaryStr) {
        boundaryStr = boundaryStr.replace("(", "").replace(")", "");

        for (String intervalStr : boundaryStr.split(",")) {
            regionList.add(new Region(chr, intervalStr));
        }
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

        public Region(String chr, String intervalStr) { // chr, start..end
            this.chr = chr;

            String[] tmp = intervalStr.split("\\W");
            start = Integer.valueOf(tmp[0]);
            end = Integer.valueOf(tmp[2]);
        }

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
