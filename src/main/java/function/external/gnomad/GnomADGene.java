package function.external.gnomad;

import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class GnomADGene {

    public float pli;
    public float pRec;
    public float oe_lof_upper;
    public float misZ;
    public float oe_lof_upper_bin;

    public StringJoiner getGeneMetricsSJ() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getFloat(pli));
        sj.add(FormatManager.getFloat(pRec));
        sj.add(FormatManager.getFloat(oe_lof_upper));
        sj.add(FormatManager.getFloat(misZ));
        sj.add(FormatManager.getFloat(oe_lof_upper_bin));

        return sj;
    }
}
