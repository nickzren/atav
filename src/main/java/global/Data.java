package global;

/**
 *
 * @author nick
 */
public class Data {

    // software info
    public static final String APP_NAME = "ATAV (Analysis Tool for Annotated Variants)";
    public static String VERSION = "dragen";
    public static String userName = Data.STRING_NA;

    // system config file path
    public static final String SYSTEM_CONFIG = "/nfs/goldstein/software/config/atav.dragen.system.config.properties";
    public static final String SYSTEM_CONFIG_FOR_DEBUG = "/nfs/goldstein/software/config/atav.dragen.debug.system.config.properties";

    // recource path for server-side debug
    public static final String RECOURCE_PATH = "";

    // recource path for client-side debug
    // public static final String RECOURCE_PATH = new File(".").getAbsolutePath() + "/src/main/resources/";
    public static final int NO_FILTER = Integer.MAX_VALUE;

    public static final byte BYTE_NA = Byte.MIN_VALUE;
    public static final short SHORT_NA = Short.MIN_VALUE;
    public static final int INTEGER_NA = Integer.MIN_VALUE;
    public static final float FLOAT_NA = Float.MIN_VALUE;
    public static final double DOUBLE_NA = Double.MIN_VALUE;
    public static final String STRING_NA = "NA";
}
