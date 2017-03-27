package function.annotation.base;

import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class Exon extends Region {

    int id;

    public Exon(int idInt, String chr, int start, int end) {
        super(chr, start, end);

        this.id = idInt;
    }

    public int getId() {
        return id;
    }
}
