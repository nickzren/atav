package function.external.pext;

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
public class PextManager {

    static final String variantTable = "pext.snv";

    private static String chr = Data.STRING_NA;
    private static int pos = Data.INTEGER_NA;
    private static String alt = Data.STRING_NA;
    private static String gene = Data.STRING_NA;
    private static float score = Data.FLOAT_NA;

    public static String getTitle() {
        return "PEXT Score";
    }

    public static String getVersion() {
        return "PEXT: " + DataManager.getVersion(variantTable) + "\n";
    }

    public static float getScore(String _chr, int _pos, String _alt, String _gene) {
        if (chr.equals(_chr) && pos == _pos && alt.equals(_alt) && gene.equals(_gene)) {
            return score;
        } else {
            chr = _chr;
            pos = _pos;
            alt = _alt;
            gene = _gene;
        }
        
        try {
            String sql = "SELECT score "
                    + "FROM " + variantTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND alt ='" + alt + "' "
                    + "AND hgnc_gene = '" + gene + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                score = rs.getFloat("score");
            } else {
                score = Data.FLOAT_NA;
            }
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return score;
    }

    public static ArrayList<Pext> getPextList(String chr, int pos, String alt) {
        ArrayList<Pext> list = new ArrayList<>();

        try {
            String sql = "SELECT hgnc_gene,score "
                    + "FROM " + variantTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND alt ='" + alt + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String gene = rs.getString("hgnc_gene");
                float score = rs.getFloat("score");

                list.add(new Pext(gene, score));
            }
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return list;
    }
}
