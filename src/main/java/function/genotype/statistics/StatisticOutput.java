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
            if (sampleCount[Index.REF][Index.ALL]
                    + sampleCount[Index.REF_MALE][Index.ALL] > 0) {
                return true;
            }
        } else {
            if (sampleCount[Index.HOM][Index.ALL]
                    + sampleCount[Index.HOM_MALE][Index.ALL] > 0) {
                return true;
            }
        }

        return false;
    }
}
