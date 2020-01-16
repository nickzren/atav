package function.external.primateai;

import function.external.base.DataManager;
import function.variant.base.Region;
import global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class PrimateAIManager {

    static final String variantTable = "PrimateAI.variant_042319";
    static final String mnvTable = "PrimateAI.mnv_042319";

    public static String getHeader() {
        return "PrimateAI";
    }

    public static String getVersion() {
        return "PrimateAI: " + DataManager.getVersion(variantTable) + "\n";
    }

    public static String getSqlByRegion(Region region) {
        return "SELECT chr,pos,ref,alt,primateDL_score as score "
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();
    }

    public static float getPrimateAI(String chr, int pos, String ref, String alt, boolean isMNV) {
        String sql = PrimateAIManager.getSqlByVariant(chr, pos, ref, alt, isMNV);

        try {
            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                return rs.getFloat("score");
            }
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return Data.FLOAT_NA;
    }

    public static String getSqlByVariant(String chr,
            int pos, String ref, String alt, boolean isMNV) {
        String table = variantTable;
        if (isMNV) {
            table = mnvTable;
        }

        return "SELECT primateDL_score as score "
                + "FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref = '" + ref + "' "
                + "AND alt = '" + alt + "'";
    }
}
