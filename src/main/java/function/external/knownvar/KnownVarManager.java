package function.external.knownvar;

import global.Data;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class KnownVarManager {

    public static final String clinvarTable = "knownvar.clinvar_2015_06_22";
    public static final String hgmdTable = "knownvar.hgmd";
    public static final String omimTable = "knownvar.omim_2015_07_15";

    public static String getTitle() {
        if (KnownVarCommand.isIncludeKnownVar) {
            return "Clinvar Clinical Significance,"
                    + "Clinvar Other Ids,"
                    + "Clinvar Disease Name,"
                    + "Clinvar Flanking Count,"
                    + "HGMD Variant Class,"
                    + "HGMD Pmid,"
                    + "HGMD Disease Name,"
                    + "HGMD Flanking Count,"
                    + "OMIM Gene Name,"
                    + "OMIM Disease Name,";
        } else {
            return "";
        }
    }

    public static String getSql4Clinvar(String chr, int pos, String ref, String alt) {
        return "SELECT ClinicalSignificance,"
                + "OtherIds,"
                + "DiseaseName "
                + "From " + clinvarTable + " "
                + "WHERE chr='" + chr + "' "
                + "AND pos=" + pos + " "
                + "AND ref='" + ref + "' "
                + "AND alt='" + alt + "'";
    }

    public static String getSql4HGMD(String chr, int pos, String ref, String alt) {
        return "SELECT variantClass,"
                + "pmid,"
                + "DiseaseName "
                + "From " + hgmdTable + " "
                + "WHERE chr='" + chr + "' "
                + "AND pos=" + pos + " "
                + "AND ref='" + ref + "' "
                + "AND alt='" + alt + "'";
    }

    public static String getSql4OMIM(String geneName) {
        return "SELECT diseaseName "
                + "From " + omimTable + " "
                + "WHERE geneName='" + geneName + "' ";
    }

    public static int getFlankingCount(boolean isSnv, String chr, int pos, String table) {
        try {
            int width = KnownVarCommand.snvWidth;

            if (!isSnv) {
                width = KnownVarCommand.indelWidth;
            }

            String sql = "SELECT count(*) as count "
                    + "From " + table + " "
                    + "WHERE chr='" + chr + "' "
                    + "AND pos BETWEEN " + (pos - width) + " AND " + (pos + width);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.NA;
    }
}
