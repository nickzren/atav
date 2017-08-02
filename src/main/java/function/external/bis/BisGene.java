package function.external.bis;

import java.util.ArrayList;
//

/**
 *
 * @author nick
 */
public class BisGene {

    private String id; // domain or exon identifier
    private String chr;
    private ArrayList<Region> regionList;
    private float score0005;
    private float score0001;
    private float score00005;
    private float score00001;

    public BisGene(String id,
            String chr,
            ArrayList<Region> regionList,
            float score0005,
            float score0001,
            float score00005,
            float score00001) {
        this.id = id;
        this.chr = chr;
        this.regionList = regionList;
        this.score0005 = score0005;
        this.score0001 = score0001;
        this.score00005 = score00005;
        this.score00001 = score00001;
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

    public float getScore0005() {
        return score0005;
    }

    public float getScore0001() {
        return score0001;
    }

    public float getScore00005() {
        return score00005;
    }

    public float getScore00001() {
        return score00001;
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
