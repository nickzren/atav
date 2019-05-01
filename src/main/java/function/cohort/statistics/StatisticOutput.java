package function.cohort.statistics;

import function.cohort.base.CalledVariant;
import function.variant.base.Output;
import global.Index;

/**
 *
 * @author nick
 */
public class StatisticOutput extends Output {

    double pValue = 0;

    public StatisticOutput(CalledVariant c) {
        super(c);
    }

    public boolean isRecessive() {
        return calledVar.genoCount[Index.HOM][Index.CASE]
                + calledVar.genoCount[Index.HOM][Index.CTRL] > 0;
    }
}
