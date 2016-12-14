package function.external.gerp;

import global.Data;

/**
 *
 * @author nick
 */
public class Gerp {

    private String chr;
    private int pos;
    private float score;

    public Gerp() {
        chr = Data.STRING_NA;
        pos = Data.INTEGER_NA;
        score = Data.FLOAT_NA;
    }

    public void setValues(String chr, int pos, float score) {
        this.chr = chr;
        this.pos = pos;
        this.score = score;
    }

    public boolean isSameSite(String chr, int pos) {
        return this.chr.equalsIgnoreCase(chr)
                && this.pos == pos;
    }

    public float getScore() {
        return score;
    }
}
