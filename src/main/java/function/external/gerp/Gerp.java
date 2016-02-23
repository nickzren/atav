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
        chr = "NA";
        pos = Data.NA;
        score = Data.NA;
    }

    public void setValues(String chr, int pos, float score) {
        this.chr = chr;
        this.pos = pos;
        this.score = score;
    }

    public boolean isSameSite(String chr, int pos) {
        if (this.chr.equalsIgnoreCase(chr)
                && this.pos == pos) {
            return true;
        }

        return false;
    }

    public float getScore() {
        return score;
    }
}
