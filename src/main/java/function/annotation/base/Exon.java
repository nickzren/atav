package function.annotation.base;

import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class Exon extends Region {

    String idStr;

    public Exon(String idStr, String chr, int start, int end) {
        super(chr, start, end);

        this.idStr = idStr;
    }

    public String getIdStr() {
        return idStr;
    }
}
