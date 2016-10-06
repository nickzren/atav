package function.external.evs;

import function.external.base.DataManager;
import function.variant.base.Region;

/**
 *
 * @author nick
 */
public class EvsManager {

    public static final String[] EVS_POP = {"ea", "aa", "all"};

    static final String coverageTable = "evs.coverage";
    static final String snvTable = "evs.snv_maf_2015_09_16";
    static final String indelTable = "evs.indel_maf_2015_09_16";

    public static String getTitle() {
        if (EvsCommand.isIncludeEvs) {
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
        } else {
            return "";
        }
    }

    public static String getVersion() {
        if (EvsCommand.isIncludeEvs) {
            return "EVS: " + DataManager.getVersion(snvTable) + "\n";
        } else {
            return "";
        }
    }

    public static String getSql4Maf(boolean isSnv, Region region) {
        String table = snvTable;

        if (!isSnv) {
            table = indelTable;
        }

        return "SELECT * "
                + "FROM " + table + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND position BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();
    }

    public static String getSql4Maf(boolean isSnv, String chr,
            int pos, String ref, String alt) {
        String table = snvTable;

        if (!isSnv) {
            table = indelTable;
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
                + "FROM " + coverageTable + " "
                + "WHERE chr = '" + chr + "' AND position = " + pos;
    }
}
