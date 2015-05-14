package atav.analysis.base;

import atav.global.Data;
import atav.manager.data.RegionManager;
import atav.manager.utils.FormatManager;

/**
 *
 * @author nick
 */
public class Region implements Comparable {

    protected int regionId;
    protected String chrStr;
    protected int chrNum;
    protected int startPosition;
    protected int endPosition;
    int length;

    public Region(int id, String chr, int start, int end) {
        init(chr, start, end);

        regionId = id;
    }

    public Region(String chr, int start, int end) {
        init(chr, start, end);

        regionId = RegionManager.getIdByChr(chrStr);
    }

    public void init(String chr, int start, int end) {
        String name = chr.toUpperCase();
        if (name.startsWith("CHR")) {
            chrStr = name.substring(3);
        } else {
            chrStr = name;
        }

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

    public int getRegionId() {
        return regionId;
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

    /*
     * inside Pseudoautosomal Region will be same as autosome
     */
    public boolean isInsideAutosomalOrPseudoautosomalRegions() {
        if (chrNum < 23 || chrNum == 26) {
            return true;
        }

        if (isInsideXPseudoautosomalRegions()
                || isInsideYPseudoautosomalRegions()) {
            return true;
        }

        return false;
    }

    /*
     * pseudoautosomal regions on sex chromosomes of hg19
     *
     * chrX:60001-2699520 and chrX:154931044-155260560
     */
    public boolean isInsideXPseudoautosomalRegions() {
        int startX1 = 60001, endX1 = 2699520;
        int startX2 = 154931044, endX2 = 155260560;

        if (chrNum == 23
                && ((startPosition >= startX1 && startPosition <= endX1)
                || (endPosition >= startX2 && endPosition <= endX2))) {
            return true;
        }

        return false;
    }

    /*
     * chrY:10001-2649520 and chrY:59034050-59363566
     */
    public boolean isInsideYPseudoautosomalRegions() {
        int startY1 = 10001, endY1 = 2649520;
        int startY2 = 59034050, endY2 = 59363566;

        if (chrNum == 24
                && ((startPosition >= startY1 && startPosition <= endY1)
                || (endPosition >= startY2 && endPosition <= endY2))) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        String chr = "chr" + chrStr;
        if (startPosition == 0 && endPosition == 0) {
            return chr;
        }

        chr += ":" + startPosition + "-" + endPosition;

        return chr;
    }

    public int compareTo(Object another) throws ClassCastException {
        Region that = (Region) another;
        return Double.compare(this.chrNum, that.chrNum); //small -> large
    }
}
