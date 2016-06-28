package utils;

import global.Data;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.math.BigDecimal;
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

    public static double devide(double a, double b) {
        if (b == 0 || b == Data.NA || a == Data.NA) {
            return Data.NA;
        } else {
            return a / b;
        }
    }

    public static double abs(double a, double b) {
        if (b == Data.NA || a == Data.NA) {
            return Data.NA;
        } else {
            return Math.abs(a - b);
        }
    }

    //    public static double roundToDecimals(double d, double c) {
//        int t = (int) (d * c + 0.5);
//
//        return (double) t / c;
//    }
    public static double roundToDecimals(double value) {
        int t = (int) (value * 100000000 + 0.5);
        double pValue = (double) t / 100000000;

        if (pValue > 0.00001) {
            return pValue;
        }

        BigDecimal temp = new BigDecimal(value);
        temp = temp.setScale(8, BigDecimal.ROUND_HALF_UP);

        return temp.doubleValue();
    }
}
