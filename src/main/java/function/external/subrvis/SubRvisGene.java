package function.external.subrvis;

import java.util.ArrayList;
//
/**
 *
 * @author nick
 */
public class SubRvisGene {

    private String id; // domain or exon identifier
    private String chr;
    private ArrayList<Region> regionList;
    private float percentile;
    private float mtrPercentile;

    public SubRvisGene(String id,
            String chr,
            ArrayList<Region> regionList,
            float percentile, 
            float mtrPercentile) {
        this.id = id;
        this.chr = chr;
        this.regionList = regionList;
        this.percentile = percentile;
        this.mtrPercentile = mtrPercentile;
    }
    
    public String getId(){
        return id;
    }

    public String getChr() {
        return chr;
    }

    public ArrayList<Region> getRegionList() {
        return regionList;
    }

    public float getPercentile() {
        return percentile;
    }
    
    public float getMTRPercentile() {
        return mtrPercentile;
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
