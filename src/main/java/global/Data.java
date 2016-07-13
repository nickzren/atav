package global;

/**
 *
 * @author nick
 */
public class Data {

    // software info
    public static final String AppTitle = "ATAV (Analysis Tool for Annotated Variants)";
    public static String version = "kaustubh";
    public static final String year = "2016";
    public static final String leadDeveloper = "Nick Ren";
    public static final String pipelineDeveloper = "Joshua Bridgers, Brett Copeland, Raghavendra Padmanabhan";
    public static final String projectManager = "Sitharthan Kamalakaran, David Goldstein";
    public static final String pastDeveloper = "Quanli Wang, Min He";
    public static final String insititue = "IGM (Institute for Genomic Medicine)";
    public static String userName = "UnspecifiedUser";

    // system config file path
    public static final String SYSTEM_CONFIG = "/nfs/goldstein/software/config/atav.system.config.properties";
    public static final String SYSTEM_CONFIG_FOR_DEBUG = "/nfs/goldstein/software/config/atav.debug.system.config.properties";
    
    // recource path for server-side debug
    public static final String RECOURCE_PATH = "";
    
    // recource path for client-side debug
    // public static final String RECOURCE_PATH = new File(".").getAbsolutePath() + "/src/main/resources/";
    
    public static final int NO_FILTER = Integer.MAX_VALUE;
    public static final int NA = Integer.MIN_VALUE;
}
