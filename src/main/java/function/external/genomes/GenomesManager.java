package function.external.genomes;

/**
 *
 * @author nick
 */
public class GenomesManager {

    public static final String[] GENOMES_POP = {"global", "eas", "eur", "afr", "amr", "sas"};

    static String snvTable = "1000g.snv_20130502";
    static String indelTable = "1000g.indel_20130502";
    
    public static String getTitle() {
        String title = "";

        if (GenomesCommand.isInclude1000Genomes) {
            for (String str : GENOMES_POP) {
                title += "1000 Genomes " + str.toUpperCase() + " maf,";
            }
        }

        return title;
    }

    public static String getSql4Maf(boolean isSnv, String chr,
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
}
