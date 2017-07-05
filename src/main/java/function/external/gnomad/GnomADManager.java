package function.external.gnomad;

import function.external.base.DataManager;
import function.variant.base.Region;

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
        String title = "";

        if (GnomADCommand.isIncludeGnomADExome) {
            for (String str : GNOMAD_EXOME_POP) {
                title += "gnomAD Exome " + str + " maf,"
                        + "gnomAD Exome " + str + " gts,";
            }

            title += "gnomAD Exome filter,"
                    + "gnomAD Exome AB MEDIAN,"
                    + "gnomAD Exome GQ MEDIAN,"
                    + "gnomAD Exome AS RF,"
                    + "gnomAD Exome Mean Coverage,"
                    + "gnomAD Exome Sample Covered 10x,";
        }

        return title;
    }

    public static String getGenomeTitle() {
        String title = "";

        if (GnomADCommand.isIncludeGnomADGenome) {
            for (String str : GNOMAD_GENOME_POP) {
                title += "gnomAD Genome " + str + " maf,"
                        + "gnomAD Genome " + str + " gts,";
            }

            title += "gnomAD Genome filter,"
                    + "gnomAD Genome AB MEDIAN,"
                    + "gnomAD Genome GQ MEDIAN,"
                    + "gnomAD Genome AS RF,";
        }

        return title;
    }

    public static String getExomeVersion() {
        if (GnomADCommand.isIncludeGnomADExome) {
            return "gnomAD Exome: " + DataManager.getVersion(exomeVariantTable) + "\n";
        } else {
            return "";
        }
    }

    public static String getGenomeVersion() {
        if (GnomADCommand.isIncludeGnomADGenome) {
            return "gnomAD Genome: " + DataManager.getVersion(genomeVariantTable) + "\n";
        } else {
            return "";
        }
    }

    public static String getSql4CvgExome(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM " + exomeCoverageTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    public static String getSql4MafExome(Region region) {
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

    public static String getSql4MafGenome(Region region) {
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

    public static String getSql4MafExome(String chr,
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

    public static String getSql4MafGenome(String chr,
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
