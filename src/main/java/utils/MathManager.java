package utils;

import global.Data;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

/**
 *
 * @author nick, quanli
 */
public class MathManager {

    private static BinomialTest BT;
    private static ScriptEngine renjinEngine;

    public static ScriptEngine getRenjinEngine() {
        if (renjinEngine == null) {
            renjinEngine = new ScriptEngineManager().getEngineByName("Renjin");

            if (renjinEngine == null) {
                ErrorManager.print("Renjin Script Engine not found on the classpath.");
            }
        }

        return renjinEngine;
    }

    public static double getBinomial(int numberOfTrials, int numberOfSuccesses, double probability) {
        if (BT == null) {
            BT = new BinomialTest();
        }

        if (numberOfTrials == Data.NA || numberOfTrials == Data.NA) {
            return Data.NA;
        }

        return BT.binomialTest(numberOfTrials,
                numberOfSuccesses,
                probability,
                AlternativeHypothesis.LESS_THAN);
    }
}
