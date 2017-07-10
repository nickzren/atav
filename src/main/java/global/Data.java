package global;

/**
 *
 * @author nick
 */
public class Data {

    // software info
    public static final String APP_NAME = "ATAV (Analysis Tool for Annotated Variants)";
    public static String VERSION = "6.6";
    public static String userName = System.getProperty("user.name");

    // system config file path
    public static final String SYSTEM_CONFIG = "/nfs/goldstein/software/config/atav.system.config.properties";
    public static final String SYSTEM_CONFIG_FOR_DEBUG = "/nfs/goldstein/software/config/atav.debug.system.config.properties";

    // atav home path
    public static final String ATAV_HOME = "/nfs/goldstein/software/atav_home/";

    public static final int NO_FILTER = Integer.MAX_VALUE;
    public static final int NA = Integer.MIN_VALUE;
}
