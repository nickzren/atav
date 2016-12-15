package function.genotype.statistics;

import function.genotype.base.CalledVariant;
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
        if (isMinorRef) {
            if (genoCount[Index.REF][Index.CASE]
                    + genoCount[Index.REF][Index.CTRL] > 0) {
                return true;
            }
        } else if (genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HOM][Index.CTRL] > 0) {
            return true;
        }

        return false;
    }
}
