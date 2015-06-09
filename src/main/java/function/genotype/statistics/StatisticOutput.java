package function.genotype.statistics;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;

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
