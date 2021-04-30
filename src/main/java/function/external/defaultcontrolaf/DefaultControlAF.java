package function.external.defaultcontrolaf;

import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class DefaultControlAF {

    int ac = Data.INTEGER_NA;
    float af = Data.FLOAT_NA;
    int nhom = Data.INTEGER_NA;

    public void init(int ac, float af, int nhom) {
        this.ac = ac;
        this.af = af;
        this.nhom = nhom;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(ac));
        sj.add(FormatManager.getFloat(af));
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
