package function.external.pext;

import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class PextOutput {

    String variantIdStr;
    float ratio;

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Variant ID");
        sj.add(PextManager.getHeader());

        return sj.toString();
    }

    public PextOutput(String id) throws Exception {
        variantIdStr = id;

        String[] tmp = id.split("-"); // chr-pos-ref-alt

        ratio = PextManager.getRatio(tmp[0], Integer.parseInt(tmp[1]));
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(variantIdStr);
        sj.add(FormatManager.getFloat(ratio));

        return sj;
    }

    public boolean isValid() {
        return PextCommand.isPextRatioValid(ratio);
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
