package global;

/**
 *
 * @author nick
 */
public class Data {

    // software info
    public static final String APP_NAME = "ATAV (Analysis Tool for Annotated Variants)";
    public static String VERSION = "trunk";
    public static String userName = System.getProperty("user.name");

    // atav home path (location of executable jar file, config dir, data dir, lib dir etc.)
    public static String ATAV_HOME = System.getenv().getOrDefault("ATAV_HOME", "");

    // system config file path
    public static final String SYSTEM_CONFIG = Data.ATAV_HOME + "config/atav.dragen.system.config.properties";
    public static final String SYSTEM_CONFIG_FOR_DEBUG = Data.ATAV_HOME + "config/atav.dragen.debug.system.config.properties";

    // system default values
    public static final int NO_FILTER = Integer.MAX_VALUE;
    public static final String NO_FILTER_STR = "";

    public static final byte BYTE_NA = Byte.MIN_VALUE;
    public static final short SHORT_NA = Short.MIN_VALUE;
    public static final int INTEGER_NA = Integer.MIN_VALUE;
    public static final float FLOAT_NA = Float.MIN_VALUE;
    public static final double DOUBLE_NA = Double.MIN_VALUE;
    public static String STRING_NA = "NA";
    public static String VCF_NA = ".";
    public static String STRING_NAN = "NaN";
}
