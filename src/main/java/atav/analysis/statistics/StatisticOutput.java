package atav.analysis.statistics;

import atav.analysis.base.CalledVariant;
import atav.analysis.base.Output;

/**
 *
 * @author nick
 */
public class StatisticOutput extends Output{

    double pValue = 0;
    
    public StatisticOutput(CalledVariant c) {
        super(c);
    }
}
