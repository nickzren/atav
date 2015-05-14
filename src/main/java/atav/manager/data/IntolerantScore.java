package atav.manager.data;

import atav.global.Data;

/**
 *
 * @author nick
 */
public class IntolerantScore {

    String geneName;
    // pop all
    double all001Value;
    double all001Percentile;
    double all01Value;
    double all01Percentile;
    double all1Value;
    double all1Percentile;
    double pp2All01Value;
    double pp2All01Percentile;
    // pop ea
    double ea01Value;
    double ea01Percentile;
    double ea1Value;
    double ea1Percentile;
    // pop aa
    double aa01Value;
    double aa01Percentile;
    double aa1Value;
    double aa1Percentile;

    double oEratioPercentile;
    String edgeCase;

    public IntolerantScore(String[] values) {
        geneName = values[0];

        all001Value = getDouble(values[1]);
        all001Percentile = getDouble(values[2]);

        all01Value = getDouble(values[3]);
        all01Percentile = getDouble(values[4]);

        all1Value = getDouble(values[5]);
        all1Percentile = getDouble(values[6]);

        pp2All01Value = getDouble(values[7]);
        pp2All01Percentile = getDouble(values[8]);

        ea01Value = getDouble(values[9]);
        ea01Percentile = getDouble(values[10]);

        ea1Value = getDouble(values[11]);
        ea1Percentile = getDouble(values[12]);

        aa01Value = getDouble(values[13]);
        aa01Percentile = getDouble(values[14]);

        aa1Value = getDouble(values[15]);
        aa1Percentile = getDouble(values[16]);

        oEratioPercentile = getDouble(values[17]);
        edgeCase = values[18];
    }

    private double getDouble(String str) {
        try {
            return Double.valueOf(str);
        } catch (NumberFormatException e) {
            return Data.NA;
        }
    }

    public String getGeneName() {
        return geneName;
    }

    public double getall01Percentile() {
        if (all01Percentile == Data.NA) {
            return Data.NA;
        }

        return all01Percentile / 100;
    }

    public double getOEratioPercentile() {
        if (oEratioPercentile == Data.NA) {
            return Data.NA;
        }

        return oEratioPercentile / 100;
    }

    public String getEdgeCase() {
        return edgeCase;
    }
}
