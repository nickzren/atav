package function.external.gnomad;

import function.external.base.DataManager;
import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class GnomADManager {

    public static final String[] GNOMAD_EXOME_POP = {"global", "afr", "amr", "asj", "eas", "sas", "fin", "nfe", "oth"};

    static final String coverageTable = "gnomad.exome_coverage_170228";
    static String variantTable = "gnomad.exome_variant_170228";

    public static void init() {
    }

    public static String getTitle() {
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

    public static String getVersion() {
        if (GnomADCommand.isIncludeGnomADExome) {
            return "gnomAD Exome: " + DataManager.getVersion(variantTable) + "\n";
        } else {
            return "";
        }
    }

    public static String getSql4Cvg(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM " + coverageTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    public static String getSqlByRegion(Region region) {
        String result = "chr,pos,ref_allele,alt_allele,";

        for (String str : GNOMAD_EXOME_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF ";

        String sql = "SELECT " + result
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }

    public static String getSqlByVariant(String chr,
            int pos, String ref, String alt) {
        String result = "";

        for (String str : GNOMAD_EXOME_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF ";

        return "SELECT " + result
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";
    }
}
