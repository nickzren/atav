package utils;

import global.Data;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

/**
 *
 * @author nick
 */
public class FormatManager {

    private static NumberFormat pformat1 = new DecimalFormat("0.####");
    private static NumberFormat pformat2 = new DecimalFormat("0.###E000");
    private static NumberFormat pformat3 = new DecimalFormat("0.######");

    public static String getDouble(double value) {
        if (value == Data.NA) {
            return "NA";
        }

        if (value < 0.001 && value > 0) {
            return pformat2.format(value);
        } else {
            return pformat1.format(value);
        }
    }

    public static String getInteger(int value) {
        if (value == Data.NA) {
            return "NA";
        }

        return String.valueOf(value);
    }

    public static String getString(String str) {
        if (str == null || str.equals("-") || str.equals("")) {
            str = "NA";
        }

        return str;
    }

    public static String getFloat(float value) {
        if (value == Data.NA) {
            return "NA";
        }

        if (value == 0.0f) {
            return "0";
        }

        return pformat3.format(value);
    }

    public static float getFloat(String str) {
        if (str == null) {
            return Data.NA;
        }

        return Float.valueOf(str);
    }

    public static String getFunction(String str) {
        if (str == null) {
            str = "INTERGENIC";
        }

        return str;
    }

    public static String getPercAltRead(double alt, int gatkFilteredCoverage) {
        if (gatkFilteredCoverage == 0
                || gatkFilteredCoverage == Data.NA) {
            return "NA";
        } else {
            return getDouble(alt / gatkFilteredCoverage);
        }
    }

    public static boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
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

    public static double devide(double a, double b) {
        if (b <= 0) {
            return Data.NA;
        } else {
            return a / b;
        }
    }

    public static int[][] deepCopyIntArray(int[][] original) {
        final int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }

        return result;
    }

    public static int[] deepCopyIntArray(int[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static double[][] deepCopyDoubleArray(double[][] original) {
        final double[][] result = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }

        return result;
    }

    public static double[] deepCopyDoubleArray(double[] original) {
        return Arrays.copyOf(original, original.length);
    }
}
