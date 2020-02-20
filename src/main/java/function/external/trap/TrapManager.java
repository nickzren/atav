package function.external.trap;

import function.external.base.DataManager;
import global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class TrapManager {

    static final String variantTable = "trap_v3.snv_chr";
    static final String mnvTable = "trap_v3.mnv";

    private static String chr = Data.STRING_NA;
    private static int pos = Data.INTEGER_NA;
    private static String alt = Data.STRING_NA;
    private static String gene = Data.STRING_NA;
    private static float score = Data.FLOAT_NA;

    public static String getHeader() {
        return "TraP Score";
    }

    public static String getVersion() {
        return "TraP: " + DataManager.getVersion(variantTable) + "\n";
    }

    public static float getScore(String _chr, int _pos, String _alt, 
            boolean isMNV, String _gene) {
        if (chr.equals(_chr) && pos == _pos && alt.equals(_alt) && gene.equals(_gene)) {
            return score;
        } else {
            chr = _chr;
            pos = _pos;
            alt = _alt;
            gene = _gene;
        }

        String table = variantTable + chr;
        if(isMNV) {
            table = mnvTable;
        }
        
        try {
            String sql = "SELECT score "
                    + "FROM " + table + " "
                    + "WHERE pos = " + pos + " "
                    + "AND alt ='" + alt + "' "
                    + "AND hgnc_gene = '" + gene + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                score = rs.getFloat("score");
            } else {
                score = Data.FLOAT_NA;
            }
            
            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return score;
    }

    public static ArrayList<Trap> getTrapList(String chr, int pos, String alt) {
        ArrayList<Trap> list = new ArrayList<>();

        try {
            String sql = "SELECT hgnc_gene,score "
                    + "FROM " + variantTable + chr + " "
                    + "WHERE pos = " + pos + " "
                    + "AND alt ='" + alt + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String gene = rs.getString("hgnc_gene");
                float score = rs.getFloat("score");

                list.add(new Trap(gene, score));
            }
            
            rs.close();
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return list;
    }
}
