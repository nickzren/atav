package function.external.evs;

/**
 *
 * @author nick
 */
public class EvsManager {

    public static String getTitle() {
        return "Evs Eur Covered Samples,"
                + "Evs Eur Average Coverage,"
                + "Evs Afr Covered Samples,"
                + "Evs Afr Average Coverage,"
                + "Evs All Covered Samples,"
                + "Evs All Average Coverage,"
                + "Evs Eur Maf," // Eur America
                + "Evs Eur Genotype Count,"
                + "Evs Afr Maf," // Afr America
                + "Evs Afr Genotype Count,"
                + "Evs All Maf,"
                + "Evs All Genotype Count,"
                + "Evs Filter Status,";
    }

    public static String getSql4Maf(boolean isSnv, String chr,
            int pos, String ref, String alt) {
        String table = "evs.snv_maf";

        if (!isSnv) {
            table = "evs.indel_maf";
        }

        return "SELECT all_maf, ea_maf, aa_maf,"
                + "all_genotype_count, ea_genotype_count, aa_genotype_count,"
                + "FilterStatus "
                + "FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND position = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";
    }

    public static String getSql4Cvg(String chr, int pos) {
        return "SELECT ALLSampleCovered, AllAvgCoverage,"
                + "EASampleCovered, EAAvgCoverage,"
                + "AASampleCovered, AAAvgCoverage "
                + "FROM evs.coverage "
                + "WHERE chr = '" + chr + "' AND position = " + pos;
    }
}
