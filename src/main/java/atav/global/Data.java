package atav.global;

/**
 *
 * @author nick
 */
public class Data {

    // software info
    public static final String AppTitle = "ATAV (Analysis Tool for Annotated Variants)";
    public static String version = "6.1 nick";
    public static final String year = "2012-2015";
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
    public static final String[] POLYPHEN_CAT = {"probably", "possibly", "unknown", "benign"};
    public static final String[] VARIANT_STATUS = {"pass", "pass+intermediate", "all"};
    public static final String DENOVO_RULES_PATH = "/nfs/goldstein/software/atav_home/data/trio_rule.txt";
    public static final String CCDS_TRANSCRIPT_PATH = "/nfs/goldstein/software/atav_home/data/ccds_transcript.txt";
    public static final String CANONICAL_TRANSCRIPT_PATH = "/nfs/goldstein/software/atav_home/data/canonical_transcript.txt";
    public static final String INTOLERANT_SCORE_PATH = "/nfs/goldstein/software/atav_home/data/intolerant_score.txt";
    public static final String ARTIFACTS_Variant_PATH = "/nfs/goldstein/software/atav_home/data/artifacts_variant.txt";
    public static final String ARTIFACTS_GENE_PATH = "/nfs/goldstein/software/atav_home/data/artifacts_gene.txt";
    public static final String GENE_ENSEMBL_PATH = "/nfs/goldstein/software/atav_home/data/gene_ensembl.txt";
    public static final String EXAMPLE_OPT_PATH = "/nfs/goldstein/software/atav_home/lib/example.opt";
    public static final String phs000473_SAMPLE_REGION_PATH = "/nfs/goldstein/software/atav_home/data/region/Agilent_SureSelectV2_Covered.50bp_padded.mergedOneBased.txt";
    public static final int MAX_VARIANT = 100000;
    public static final int NO_FILTER = Integer.MAX_VALUE;
    public static final int NA = Integer.MIN_VALUE;

    public static final int COVERAGE_BLOCK_SIZE = 1024;

}
