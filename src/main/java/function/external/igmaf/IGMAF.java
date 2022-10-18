package function.external.igmaf;

import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author jaimee
 */
public class IGMAF {

    int ac = Data.INTEGER_NA;
    float af = Data.FLOAT_NA;
    int ns = Data.INTEGER_NA;
    int nhom = Data.INTEGER_NA;
    
    public void init(int ac, float af, int ns, int nhom) {
        this.ac = ac;
        this.af = af;
        this.ns = ns;
        this.nhom = nhom;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(ac));
        sj.add(FormatManager.getFloat(af));
        sj.add(FormatManager.getInteger(ns));
        sj.add(FormatManager.getInteger(nhom));

        return sj;
    }

    public float getAF() {
        return this.af;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
