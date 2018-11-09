package function.external.gnomad;

import function.external.base.DataManager;
import function.variant.base.Region;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class GnomADManager {

    public static final String[] GNOMAD_EXOME_POP = {
        "global", "controls", "non_neuro",
        "afr", "amr", "asj", "eas", "sas", "fin", "nfe", "nfemax", "easmax",
        "controls_afr", "controls_amr", "controls_asj", "controls_eas", "controls_sas", "controls_fin", "controls_nfe", "controls_nfemax", "controls_easmax",
        "non_neuro_afr", "non_neuro_amr", "non_neuro_asj", "non_neuro_eas", "non_neuro_sas", "non_neuro_fin", "non_neuro_nfe", "non_neuro_nfemax", "non_neuro_easmax"
    };

    public static final String[] GNOMAD_GENOME_POP = {"global", "afr", "amr", "asj", "eas", "fin", "nfe", "oth"};

    private static final String exomeVariantTable = "gnomad_2_1.exome_variant";
    private static final String exomeMNVTable = "gnomad.exome_mnv_170228";
    private static final String genomeVariantTable = "gnomad.genome_variant_chr1_170228";
    private static final String genomeMNVTable = "gnomad.genome_mnv_170228";

    public static void init() {
    }

    public static String getExomeTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("gnomAD Exome FILTER");
        sj.add("gnomAD Exome segdup");
        sj.add("gnomAD Exome lcr");
        sj.add("gnomAD Exome decoy");
        sj.add("gnomAD Exome rf_tp_probability");
        sj.add("gnomAD Exome qd");

        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            String pop = GnomADManager.GNOMAD_EXOME_POP[i];
            sj.add("gnomAD Exome " + pop + "_AF");
            
            switch (i) {
                case 0: // global
                case 1: // controls
                case 2: // non_neuro
                    sj.add("gnomAD Exome " + pop + "_AN");
                    sj.add("gnomAD Exome " + pop + "_nhet");
                    sj.add("gnomAD Exome " + pop + "_nhomalt");
                    sj.add("gnomAD Exome " + pop + "_nhemi");
                    break;
                default:
                    break;
            }
        }

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
            int pos, String ref, String alt, boolean isMNV) {
        String result = "";

        for (String str : GNOMAD_EXOME_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF";

        String table = exomeVariantTable;
        if (isMNV) {
            table = exomeMNVTable;
        }

        String sql = "SELECT " + result + " FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";

        return sql;
    }

    public static String getSql4GenomeVariant(String chr,
            int pos, String ref, String alt, boolean isMNV) {
        String result = "";

        for (String str : GNOMAD_GENOME_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "filter,AB_MEDIAN,GQ_MEDIAN,AS_RF";

        String table = "gnomad.genome_variant_chr" + chr + "_170228";
        if (isMNV) {
            table = genomeMNVTable;
        }

        String sql = "SELECT " + result + " FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";

        return sql;
    }
}
