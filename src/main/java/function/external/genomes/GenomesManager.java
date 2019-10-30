package function.external.genomes;

import function.external.base.DataManager;
import function.variant.base.Region;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class GenomesManager {

    public static final String[] GENOMES_POP = {"global", "eas", "eur", "afr", "amr", "sas"};

    static String snvTable = "1000g.snv_20130502";
    static String indelTable = "1000g.indel_20130502";

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        for (String str : GENOMES_POP) {
            sj.add("1000 Genomes " + str.toUpperCase() + " af");
        }

        return sj.toString();
    }

    public static String getVersion() {
        return "1000 Genomes: " + DataManager.getVersion(snvTable) + "\n";
    }

    public static String getSql4AF(boolean isSnv, String chr,
            int pos, String ref, String alt) {
        String sql = "SELECT * ";

        if (isSnv) {
            sql += "FROM " + snvTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND alt_allele = '" + alt + "'";
        } else {
            sql += "FROM " + indelTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND ref_allele = '" + ref + "' "
                    + "AND alt_allele = '" + alt + "'";
        }

        return sql;
    }

    public static String getSql4AF(boolean isIndel, Region region) {
        String table = snvTable;

        if (isIndel) {
            table = indelTable;
        }

        String sql = "SELECT * FROM " + table + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }
}
