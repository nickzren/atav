package function.variant.base;

import global.Data;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class Region implements Comparable {

    protected String chrStr;
    protected int chrNum;
    protected int startPosition;
    protected int endPosition;
    protected int length;

    public Region() {
    }

    public Region(String chr, int start, int end) {
        initRegion(chr, start, end);
    }

    public void initRegion(String chr, int start, int end) {
        chrStr = chr;

        chrNum = intChr();

        startPosition = start;
        endPosition = end;

        length = endPosition - startPosition + 1;
    }

    private int intChr() {
        if (chrStr.equals("X")
                || chrStr.equals("XY")) {
            return 23;
        } else if (chrStr.equals("Y")) {
            return 24;
        } else if (chrStr.equals("MT")) {
            return 26;
        } else if (FormatManager.isInteger(chrStr)) {
            return Integer.parseInt(chrStr);
        } else {
            return Data.NA;
        }
    }

    public String getChrStr() {
        return chrStr;
    }

    public int getChrNum() {
        return chrNum;
    }

    public void setStartPosition(int start) {
        startPosition = start;
    }

    public void setEndPosition(int end) {
        endPosition = end;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setLength(int len) {
        length = len;
    }

    public int getLength() {
        return length;
    }

    public boolean isAutosome() {
        return chrNum < 23 || chrNum == 26;
    }

    /*
     * inside Pseudoautosomal Region will be same as autosome
     */
    public boolean isInsideAutosomalOrPseudoautosomalRegions() {
        if (isAutosome()) {
            return true;
        }

        return isInsideXPseudoautosomalRegions()
                || isInsideYPseudoautosomalRegions();
    }

    /*
     * pseudoautosomal regions on sex chromosomes of hg19
     *
     * chrX:60001-2699520 and chrX:154931044-155260560
     */
    public boolean isInsideXPseudoautosomalRegions() {
        int startX1 = 60001, endX1 = 2699520;
        int startX2 = 154931044, endX2 = 155260560;

        return chrNum == 23
                && ((startPosition >= startX1 && endPosition <= endX1)
                || (startPosition >= startX2 && endPosition <= endX2));
    }

    /*
     * chrY:10001-2649520 and chrY:59034050-59363566
     */
    public boolean isInsideYPseudoautosomalRegions() {
        int startY1 = 10001, endY1 = 2649520;
        int startY2 = 59034050, endY2 = 59363566;

        return chrNum == 24
                && ((startPosition >= startY1 && endPosition <= endY1)
                || (startPosition >= startY2 && endPosition <= endY2));
    }

    public boolean contains(Region r) {
        return r.getChrStr().equalsIgnoreCase(chrStr)
                && r.getStartPosition() >= startPosition
                && r.getStartPosition() <= endPosition;
    }

    public Region intersect(int start, int end) {
        if (end >= startPosition && start <= endPosition) {
            int newstart = Math.max(startPosition, start);
            int newend = Math.min(endPosition, end);
            return new Region(chrStr, newstart, newend);
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

    @Override
    public String toString() {
        String chr = "chr" + chrStr;
        if (startPosition == Data.NA && endPosition == Data.NA) {
            return chr;
        }

        chr += ":" + startPosition + "-" + endPosition;

        return chr;
    }

    @Override
    public int compareTo(Object another) throws ClassCastException {
        Region that = (Region) another;
        return Double.compare(this.chrNum, that.chrNum); //small -> large
    }
}
