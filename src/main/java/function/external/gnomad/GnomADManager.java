package function.external.gnomad;

import function.external.base.DataManager;
import function.variant.base.Region;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class GnomADManager {

    public static final String[] GNOMAD_EXOME_POP = {"global", "afr", "amr", "asj", "eas", "sas", "fin", "nfe", "oth"};
    public static final String[] GNOMAD_GENOME_POP = {"global", "afr", "amr", "asj", "eas", "fin", "nfe", "oth"};

    private static final String exomeCoverageTable = "gnomad.exome_coverage_170228";
    private static final String exomeVariantTable = "gnomad.exome_variant_170228";
    private static final String genomeVariantTable = "gnomad.genome_variant_chr1_170228";

    public static void init() {
    }

    public static String getExomeTitle() {
        StringJoiner sj = new StringJoiner(",");

        for (String str : GNOMAD_EXOME_POP) {
            sj.add("gnomAD Exome " + str + " af");
            sj.add("gnomAD Exome " + str + " gts");
        }

        sj.add("gnomAD Exome filter");
        sj.add("gnomAD Exome AB MEDIAN");
        sj.add("gnomAD Exome GQ MEDIAN");
        sj.add("gnomAD Exome AS RF");
        sj.add("gnomAD Exome Mean Coverage");
        sj.add("gnomAD Exome Sample Covered 10x");

        return sj.toString();
    }

    public static String getGenomeTitle() {
        StringJoiner sj = new StringJoiner(",");

        for (String str : GNOMAD_GENOME_POP) {
            sj.add("gnomAD Genome " + str + " af");
            sj.add("gnomAD Genome " + str + " gts");
        }

        sj.add("gnomAD Genome filter");
        sj.add("gnomAD Genome AB MEDIAN");
        sj.add("gnomAD Genome GQ MEDIAN");
        sj.add("gnomAD Genome AS RF");

        return sj.toString();
    }

    public static String getExomeVersion() {
        return "gnomAD Exome: " + DataManager.getVersion(exomeVariantTable) + "\n";
    }

    public static String getGenomeVersion() {
        return "gnomAD Genome: " + DataManager.getVersion(genomeVariantTable) + "\n";
    }

    public static String getSql4CvgExome(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM " + exomeCoverageTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    public static String getSql4ExomeVariant(Region region) {
        String result = "chr,pos,ref_allele,alt_allele,";

        for (String str : GNOMAD_EXOME_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF ";

        String sql = "SELECT " + result + "FROM " + exomeVariantTable + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }

    public static String getSql4GenomeVariant(Region region) {
        String result = "chr,pos,ref_allele,alt_allele,";

        for (String str : GNOMAD_GENOME_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF ";

        String sql = "SELECT " + result + "FROM gnomad.genome_variant_chr" + region.getChrStr() + "_170228 "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }

    public static String getSql4ExomeVariant(String chr,
            int pos, String ref, String alt) {
        String result = "";

        for (String str : GNOMAD_EXOME_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF ";

        String sql = "SELECT " + result + "FROM " + exomeVariantTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";

        return sql;
    }

    public static String getSql4GenomeVariant(String chr,
            int pos, String ref, String alt) {
        String result = "";

        for (String str : GNOMAD_GENOME_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF ";

        String sql = "SELECT " + result + "FROM gnomad.genome_variant_chr" + chr + "_170228 "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";

        return sql;
    }
}
