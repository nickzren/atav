package function.external.mpc;

import global.Data;

/**
 *
 * @author nick
 */
public class MPC {
    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private float score;
    

    public MPC() {
        chr = Data.STRING_NA;
        pos = Data.INTEGER_NA;
        score = Data.FLOAT_NA;
    }

    public void setValues(String chr, int pos, String ref, String alt, float score) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        
        this.score = score;
    }

    public float getScore() {
        return score;
    }
}
