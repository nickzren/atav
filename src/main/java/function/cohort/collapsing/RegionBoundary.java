package function.cohort.collapsing;

/**
 *
 * @author nick
 */
public class RegionBoundary {

    private String name;
    private String chr;
    private int[][] intervalAarry; // array of start, end intervals

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

    public String getChr() {
        return chr;
    }

    public String getRegionStrByIndex(int i) {
        StringBuilder sb = new StringBuilder();

        sb.append(chr);
        sb.append(":");
        sb.append(intervalAarry[i][0]);
        sb.append("-");
        sb.append(intervalAarry[i][1]);

        return sb.toString();
    }

    public int[][] getIntevalArray() {
        return intervalAarry;
    }

    private void initRegionList(String chr, String boundaryStr) {
        this.chr = chr;

        boundaryStr = boundaryStr.replace("(", "").replace(")", "");

        String[] intervalStrArray = boundaryStr.split(",");
        intervalAarry = new int[intervalStrArray.length][2];

        for (int i = 0; i < intervalStrArray.length; i++) {
            String[] tmp = intervalStrArray[i].split("\\W");

            intervalAarry[i][0] = Integer.valueOf(tmp[0]); // start
            intervalAarry[i][1] = Integer.valueOf(tmp[2]); // end
        }
    }

    private void initRegionList(String boundaryStr) {
        String[] intervalStrArray = boundaryStr.split(",");

        for (int i = 0; i < intervalStrArray.length; i++) {
            String[] tmp = intervalStrArray[i].split(":|-");

            chr = tmp[0];
            intervalAarry[i][0] = Integer.valueOf(tmp[1]); // start
            intervalAarry[i][1] = Integer.valueOf(tmp[2]); // end
        }
    }

    public boolean isContained(int pos) {
        // if pos less than first start and last end then false
        if (pos < intervalAarry[0][0] || pos > intervalAarry[intervalAarry.length - 1][1]) {
            return false;
        }

        for (int i = 0; i < intervalAarry[0].length; i++) {
            if (pos >= intervalAarry[i][0]
                    && pos <= intervalAarry[i][1]) {
                return true;
            }
        }

        return false;
    }
}
