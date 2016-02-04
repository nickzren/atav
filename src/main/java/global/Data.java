package global;

/**
 *
 * @author nick
 */
public class Data {

    // software info
    public static final String AppTitle = "ATAV (Analysis Tool for Annotated Variants)";
    public static String version = "6.3.3";
    public static final String year = "2012-2016";
    public static final String developer = "Nick Ren, Quanli Wang";
    public static final String insititue = "IGM (Institute for Genomic Medicine)";
    public static String userName = "UnspecifiedUser";

    // data info
    public static final String[] SAMPLE_TYPE = {"genome", "exome"};
    public static final String[] VARIANT_TYPE = {"snv", "indel"};
    public static String ALL_SAMPLE_ID_TABLE = "all_sample_id";
    public static String GENOME_SAMPLE_ID_TABLE = "genome_sample_id";
    public static String EXOME_SAMPLE_ID_TABLE = "exome_sample_id";
    public static final String[] ALL_CHR = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"};
    //"ea,aa,all"
    public static final String[] EVS_POP = {"ea", "aa", "all"};
    public static final String[] EXAC_POP = {"global", "afr", "amr", "eas", "sas", "fin", "nfe", "oth"};
    public static final String[] EXAC_SUBSET = {"nonpsych", "nonTCGA"};
    public static final String[] POLYPHEN_CAT = {"probably", "possibly", "unknown", "benign"};
    public static final String[] VARIANT_STATUS = {"pass", "pass+intermediate", "all"};

    // system config file path
    public static final String SYSTEM_CONFIG = "/nfs/goldstein/software/config/atav.system.config.properties";
    public static final String SYSTEM_CONFIG_FOR_DEBUG = "/nfs/goldstein/software/config/atav.debug.system.config.properties";
    
    // recource path for server-side debug
    public static final String RECOURCE_PATH = "";
    
    // recource path for client-side debug
    // public static final String RECOURCE_PATH = new File(".").getAbsolutePath() + "/src/main/resources/";
    
    public static final String DB_HOST_CONFIG_PATH = "config/host";
    public static final String SAMPLE_GROUP_RESTRICTION_PATH = "config/sample.group.restriction.txt";
    public static final String USER_GROUP_RESTRICTION_PATH = "config/user.group.restriction.txt";
    public static final String DENOVO_RULES_PATH = "data/trio_rule.txt";
    public static final String CCDS_TRANSCRIPT_PATH = "data/ccds_transcript.txt";
    public static final String CANONICAL_TRANSCRIPT_PATH = "data/canonical_transcript.txt";
    public static final String INTOLERANT_SCORE_PATH = "data/intolerant_score.txt";
    public static final String ARTIFACTS_Variant_PATH = "data/artifacts_variant.txt";
    public static final String ARTIFACTS_GENE_PATH = "data/artifacts_gene.txt";
    public static final String GENE_ENSEMBL_PATH = "data/gene_ensembl.txt";
    public static final String EXAMPLE_OPT_PATH = "lib/example.opt";
    
    public static final int MAX_VARIANT = 100000;
    public static final int NO_FILTER = Integer.MAX_VALUE;
    public static final int NA = Integer.MIN_VALUE;

    public static final int COVERAGE_BLOCK_SIZE = 1024;
}
