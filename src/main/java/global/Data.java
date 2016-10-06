package global;

import java.io.File;

/**
 *
 * @author nick
 */
public class Data {

    // software info
    public static final String APP_NAME = "ATAV (Analysis Tool for Annotated Variants)";
    public static String VERSION = "pgm_beta";
    public static String userName = "NA";

    // system config file path
    public static final String SYSTEM_CONFIG = "/nfs/goldstein/software/config/atav.pgm.system.config.properties";
    public static final String RECOURCE_PATH = new File(".").getAbsolutePath() + "/src/main/resources/";
    
    public static final int NO_FILTER = Integer.MAX_VALUE;
    public static final int NA = Integer.MIN_VALUE;
}
