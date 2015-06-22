package utils;

import global.Data;
import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

/**
 *
 * @author nick, quanli
 */
public class MathManager {

    private static final BinomialTest BT = new BinomialTest();

    public static double getBinomial(int numberOfTrials, int numberOfSuccesses, double probability) {
        if (numberOfTrials == Data.NA || numberOfTrials == Data.NA) {
            return Data.NA;
        }

        return BT.binomialTest(numberOfTrials,
                numberOfSuccesses,
                probability,
                AlternativeHypothesis.LESS_THAN);
    }
}
