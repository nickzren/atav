package function.external.exac;

import global.Data;
import utils.DBManager;
import utils.ErrorManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nick
 */
public class ExacManager {

    public static String getTitle() {
        String title = "";

        for (String str : Data.EXAC_POP) {
            title += "ExAC " + str + " maf,"
                    + "ExAC " + str + " gts,";
        }

        title += "ExAC vqslod,"
                + "ExAC Mean Coverage,"
                + "ExAC Sample Covered 10x,";

        return title;
    }

    public static Exac getExac(boolean isSnv, String chr, 
            int pos, String ref, String alt) {
        Exac exac = new Exac();

        try {
            initCvg(exac, chr, pos);

            initMaf(exac, isSnv, chr, pos, ref, alt);
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return exac;
    }

    private static void initCvg(Exac exac, String chr, int pos) throws SQLException {
        String sql = getCvgSql(chr, pos);

        ResultSet rs = DBManager.executeQuery(sql);

        if (rs.next()) {
            exac.initCvg(rs);
        }

        rs.close();
    }

    private static void initMaf(Exac exac, boolean isSnv, String chr, 
            int pos, String ref, String alt) throws SQLException {
        String sql = getMafSql(isSnv, chr, pos, ref, alt);

        ResultSet rs = DBManager.executeQuery(sql);

        if (rs.next()) {
            exac.initMaf(rs);
        } else {
            if (exac.getMeanCoverage() > 0) {
                exac.resetMaf();
            }
        }

        rs.close();
    }

    private static String getCvgSql(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM exac.coverage_03 "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    private static String getMafSql(boolean isSnv, String chr, 
            int pos, String ref, String alt) {
        String result = "";

        for (String str : Data.EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";

        String sql = "SELECT " + result;

        if (isSnv) {
            sql += "FROM exac.snv_maf_r03 "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND alt_allele = '" + alt + "'";
        } else {
            sql += "FROM exac.indel_maf_r03 "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND ref_allele = '" + ref + "' "
                    + "AND alt_allele = '" + alt + "'";
        }

        return sql;
    }
}
