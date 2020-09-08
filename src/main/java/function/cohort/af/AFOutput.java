package function.cohort.af;

import function.cohort.base.CalledVariant;
import function.variant.base.Output;
import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class AFOutput extends Output {

    public static String getHeader() {
        StringJoiner sj = new StringJoiner("\t");

        sj.add("variant_id");
        sj.add("Chr");
        sj.add("Pos");
        sj.add("Ref");
        sj.add("Alt");
        sj.add("AC");
        sj.add("AN");
        sj.add("AF");
        sj.add("NS");
        sj.add("NHOM");

        return sj.toString();
    }

    public AFOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\t");

        sj.add(FormatManager.getInteger(calledVar.variantId));
        sj.add(calledVar.getChrStr());
        sj.add(FormatManager.getInteger(calledVar.getStartPosition()));
        sj.add(calledVar.getRefAllele());
        sj.add(calledVar.getAllele());
        sj.add(FormatManager.getInteger(calledVar.getAC()));
        sj.add(FormatManager.getInteger(calledVar.getAN()));
        sj.add(FormatManager.getFloat(calledVar.getAF()));
        sj.add(FormatManager.getInteger(calledVar.getNS()));
        sj.add(FormatManager.getInteger(calledVar.getNHOM()));

        return sj.toString();
    }
}
