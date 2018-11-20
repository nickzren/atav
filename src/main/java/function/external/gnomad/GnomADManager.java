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

    public static final String[] GNOMAD_GENOME_POP = {
        "global", "controls", "non_neuro",
        "afr", "amr", "asj", "eas", "fin", "nfe", "nfemax", "easmax",
        "controls_afr", "controls_amr", "controls_asj", "controls_eas", "controls_fin", "controls_nfe", "controls_nfemax", "controls_easmax",
        "non_neuro_afr", "non_neuro_amr", "non_neuro_asj", "non_neuro_eas", "non_neuro_fin", "non_neuro_nfe", "non_neuro_nfemax", "non_neuro_easmax"
    };

    private static final String exomeVariantTable = "gnomad_2_1.exome_variant";
    private static final String exomeMNVTable = "gnomad_2_1.exome_mnv";
    private static final String genomeVariantTable = "gnomad_2_1.genome_variant_chr";
    private static final String genomeMNVTable = "gnomad_2_1.genome_mnv";

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
        sj.add("gnomAD Exome pab_max");

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

        sj.add("gnomAD Genome FILTER");
        sj.add("gnomAD Genome segdup");
        sj.add("gnomAD Genome lcr");
        sj.add("gnomAD Genome decoy");
        sj.add("gnomAD Genome rf_tp_probability");
        sj.add("gnomAD Genome qd");
        sj.add("gnomAD Genome pab_max");

        for (int i = 0; i < GnomADManager.GNOMAD_GENOME_POP.length; i++) {
            String pop = GnomADManager.GNOMAD_GENOME_POP[i];
            sj.add("gnomAD Genome " + pop + "_AF");
            
            switch (i) {
                case 0: // global
                case 1: // controls
                case 2: // non_neuro
                    sj.add("gnomAD Genome " + pop + "_AN");
                    sj.add("gnomAD Genome " + pop + "_nhet");
                    sj.add("gnomAD Genome " + pop + "_nhomalt");
                    sj.add("gnomAD Genome " + pop + "_nhemi");
                    break;
                default:
                    break;
            }
        }

        return sj.toString();
    }

    public static String getExomeVersion() {
        return "gnomAD Exome: " + DataManager.getVersion(exomeVariantTable) + "\n";
    }

    public static String getGenomeVersion() {
        return "gnomAD Genome: " + DataManager.getVersion(genomeVariantTable) + "\n";
    }

    public static String getSql4ExomeVariant(Region region) {
        String sql = "SELECT * FROM " + exomeVariantTable + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }

    public static String getSql4GenomeVariant(Region region) {
        String sql = "SELECT * FROM " + genomeVariantTable + region.getChrStr() + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }

    public static String getSql4ExomeVariant(String chr,
            int pos, String ref, String alt, boolean isMNV) {
        String table = exomeVariantTable;
        if (isMNV) {
            table = exomeMNVTable;
        }

        String sql = "SELECT * FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref = '" + ref + "' "
                + "AND alt = '" + alt + "'";

        return sql;
    }

    public static String getSql4GenomeVariant(String chr,
            int pos, String ref, String alt, boolean isMNV) {
        String table = genomeVariantTable + chr;
        if (isMNV) {
            table = genomeMNVTable;
        }

        String sql = "SELECT * FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref = '" + ref + "' "
                + "AND alt = '" + alt + "'";

        return sql;
    }
}
