package function.external.ccr;

import java.util.ArrayList;
//

/**
 *
 * @author nick
 */
public class CCRGene {

    private String id; // region identifier
    private String chr;
    private ArrayList<Region> regionList;
    private float percentiles;

    public CCRGene(String id,
            String chr,
            ArrayList<Region> regionList,
            float percentiles) {
        this.id = id;
        this.chr = chr;
        this.regionList = regionList;
        this.percentiles = percentiles;
    }

    public String getId() {
        return id;
    }

    public String getChr() {
        return chr;
    }

    public ArrayList<Region> getRegionList() {
        return regionList;
    }

    public float getPercentiles() {
        return percentiles;
    }

    public boolean isPositionIncluded(String chr, int pos) {
        if (this.chr.equals(chr)) {
            for (Region region : regionList) {
                if (region.isIncluded(pos)) {
                    return true;
                }
            }
        }

        return false;
    }
}
