package function.external.primateai;

import function.external.base.DataManager;
import global.Data;
import java.sql.PreparedStatement;
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

    private static PreparedStatement preparedStatement4Variant;
    private static PreparedStatement preparedStatement4MNV;
    private static PreparedStatement preparedStatement4Region;

    public static void init() {
        if (PrimateAICommand.isInclude) {
            String sql = "SELECT primateDL_score as score "
                    + "FROM " + variantTable + " WHERE chr=? AND pos=? AND ref=? AND alt=?";
            preparedStatement4Variant = DBManager.initPreparedStatement(sql);

            sql = "SELECT primateDL_score as score "
                    + "FROM " + mnvTable + " WHERE chr=? AND pos=? AND ref=? AND alt=?";
            preparedStatement4MNV = DBManager.initPreparedStatement(sql);
            
            sql =  "SELECT chr,pos,ref,alt,primateDL_score as score "
                + "FROM " + variantTable + " WHERE chr=? AND pos BETWEEN ? AND ?";
            preparedStatement4Region = DBManager.initPreparedStatement(sql);
        }
    }

    public static String getHeader() {
        return "PrimateAI";
    }

    public static String getVersion() {
        return "PrimateAI: " + DataManager.getVersion(variantTable) + "\n";
    }

    public static float getPrimateAI(String chr, int pos, String ref, String alt, boolean isMNV) {
        try {
            PreparedStatement preparedStatement = isMNV ? preparedStatement4MNV : preparedStatement4Variant;
            preparedStatement.setString(1, chr);
            preparedStatement.setInt(2, pos);
            preparedStatement.setString(3, ref);
            preparedStatement.setString(4, alt);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getFloat("score");
            }

            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return Data.FLOAT_NA;
    }
    
    public static PreparedStatement getPreparedStatement4Region() {
        return preparedStatement4Region;
    }
}
