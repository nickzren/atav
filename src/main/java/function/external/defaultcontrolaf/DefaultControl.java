package function.external.defaultcontrolaf;

import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class DefaultControl {

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
    
    public int getAC() {
        return this.ac == Data.INTEGER_NA ? 0 : this.ac;
    }

    public float getAF() {
        return this.af;
    }

    public int getNHOM() {
        return this.nhom == Data.INTEGER_NA ? 0 : this.nhom;
    }

    public boolean isNotObservedInControlHemiOrHom() {
        return this.nhom == Data.INTEGER_NA || this.nhom == 0;
    }

    public boolean isObservedInControlHetValid(int nHet) {
        return this.ac <= nHet && (this.nhom == Data.INTEGER_NA || this.nhom == 0);
    }

    public int getControlNHET() {
        int ac = this.ac == Data.INTEGER_NA ? 0 : this.ac;
        int nhom = this.nhom == Data.INTEGER_NA ? 0 : this.nhom;

        return ac - 2 * nhom;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
