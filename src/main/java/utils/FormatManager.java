package utils;

import global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author nick
 */
public class FormatManager {

    public static String getDouble(double value) {
        if (value == Data.DOUBLE_NA) {
            return Data.STRING_NA;
        }

        return String.valueOf(value);
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

    public static int getInteger(String value) {
        if (value.equals(Data.STRING_NA)) {
            return Data.INTEGER_NA;
        }

        return Integer.valueOf(value);
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

        return String.valueOf(value);
    }

    public static float getFloat(CSVRecord record, String column) {
        if (record.isMapped(column)) {
            return getFloat(record.get(column));
        } else {
            return Data.FLOAT_NA;
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

    public static Boolean getBoolean(CSVRecord record, String column) {
        if (record.isMapped(column)) {
            return getBoolean(record.get(column));
        } else {
            return null;
        }
    }

    public static String getBoolean(Boolean value) {
        if (value == null) {
            return Data.STRING_NA;
        }

        return String.valueOf(value);
    }

    public static Boolean getBoolean(String value) {
        if (value.equals(Data.STRING_NA)) {
            return null;
        }

        return Boolean.valueOf(value);
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

    public static String appendDoubleQuote(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        sb.append(value);
        sb.append("\"");

        return sb.toString();
    }
}
