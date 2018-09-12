package function.external.evs;

import function.external.base.DataManager;
import function.variant.base.Region;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class EvsManager {

    static final String variantTable = "evs.variant_2015_09_16";

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Evs All Maf");
        sj.add("Evs All Genotype Count");
        sj.add("Evs Filter Status");

        return sj.toString();
    }

    public static String getVersion() {
        return "EVS: " + DataManager.getVersion(variantTable) + "\n";
    }

    public static String getSqlByRegion(Region region) {
        return "SELECT chr,position,ref_allele,alt_allele,"
                + "all_maf,all_genotype_count,FilterStatus "
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND position BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();
    }

    public static String getSqlByVariant(String chr,
            int pos, String ref, String alt) {
        return "SELECT all_maf,all_genotype_count,FilterStatus "
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND position = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";
    }
}
