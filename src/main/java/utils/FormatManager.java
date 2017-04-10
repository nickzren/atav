package utils;

import global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static String getDouble(double value) {
        if (value == Data.DOUBLE_NA) {
            return Data.STRING_NA;
        }

        if (value < 0.001 && value > 0) {
            return pformat2.format(value);
        } else {
            return pformat1.format(value);
        }
    }

    public static String getByte(byte value) {
        if (value == Data.BYTE_NA) {
            return Data.STRING_NA;
        }

        return String.valueOf(value);
    }

    public static String getShort(short value) {
        if (value == Data.SHORT_NA) {
            return Data.STRING_NA;
        }

        return String.valueOf(value);
    }

    public static String getInteger(int value) {
        if (value == Data.INTEGER_NA) {
            return Data.STRING_NA;
        }

        return String.valueOf(value);
    }

    public static byte getByte(ResultSet rs, String strColName) throws SQLException {
        byte nValue = rs.getByte(strColName);
        return rs.wasNull() ? Data.BYTE_NA : nValue;
    }

    public static int getInt(ResultSet rs, String strColName) throws SQLException {
        int nValue = rs.getInt(strColName);
        return rs.wasNull() ? Data.INTEGER_NA : nValue;
    }

    public static String getString(String str) {
        if (str == null) {
            str = Data.STRING_NA;
        }

        return str;
    }

    public static String getFloat(float value) {
        if (value == Data.FLOAT_NA) {
            return Data.STRING_NA;
        }

        if (value == 0.0f) {
            return "0";
        }

        if (value < 0.001 && value > 0) {
            return pformat2.format(value);
        } else {
            return pformat1.format(value);
        }
    }

    public static float getFloat(String str) {
        if (str == null || str.equals(Data.STRING_NA)) {
            return Data.FLOAT_NA;
        }

        return Float.valueOf(str);
    }

    public static float getFloat(ResultSet rs, String strColName) throws SQLException {
        float nValue = rs.getFloat(strColName);
        return rs.wasNull() ? Data.FLOAT_NA : nValue;
    }

    public static String getFunction(String str) {
        if (str == null) {
            str = "INTERGENIC";
        }

        return str;
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

    public static int[][] deepCopyArray(int[][] original) {
        final int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }

        return result;
    }

    public static int[] deepCopyArray(int[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static float[][] deepCopyArray(float[][] original) {
        final float[][] result = new float[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }

        return result;
    }

    public static float[] deepCopyArray(float[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static double[] deepCopyArray(double[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static void deleteLastComma(StringBuilder sb) {
        if (sb.length() != 0) {
            int lastCommaIndex = sb.lastIndexOf(",");

            if (lastCommaIndex == (sb.length() - 1)) {
                sb.deleteCharAt(lastCommaIndex);
            }
        }
    }
}
