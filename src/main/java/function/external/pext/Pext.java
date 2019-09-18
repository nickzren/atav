package function.external.pext;

import global.Data;

/**
 *
 * @author nick
 */
public class Pext {

    private String chr;
    private int pos;
    private float ratio;

    public Pext() {
        chr = Data.STRING_NA;
        pos = Data.INTEGER_NA;
        ratio = Data.FLOAT_NA;
    }

    public void setValues(String chr, int pos, float ratio) {
        this.chr = chr;
        this.pos = pos;
        this.ratio = ratio;
    }

    public boolean isSameSite(String chr, int pos) {
        return this.chr.equalsIgnoreCase(chr)
                && this.pos == pos;
    }

    public float getRatio() {
        return ratio;
    }
}
