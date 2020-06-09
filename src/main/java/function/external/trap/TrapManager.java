package function.external.trap;

import function.external.base.DataManager;
import function.variant.base.RegionManager;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.csv.CSVRecord;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

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

    private static final HashMap<String, PreparedStatement> preparedStatement4VariantGeneMap = new HashMap<>();
    private static PreparedStatement preparedStatement4MNVGene;
    private static final HashMap<String, PreparedStatement> preparedStatement4VariantMap = new HashMap<>();

    public static void init() {
        if (TrapCommand.isInclude) {
            String sql = "SELECT score FROM " + mnvTable + " WHERE pos=? AND alt=? AND hgnc_gene=?";
            preparedStatement4MNVGene = DBManager.initPreparedStatement(sql);

            for (String chr : RegionManager.ALL_CHR) {
                sql = "SELECT score FROM " + variantTable + chr + " WHERE pos=? AND alt=? AND hgnc_gene=?";
                preparedStatement4VariantGeneMap.put(chr, DBManager.initPreparedStatement(sql));
                
                sql = "SELECT hgnc_gene,score FROM " + variantTable + chr + " WHERE pos=? AND alt=?";
                preparedStatement4VariantMap.put(chr, DBManager.initPreparedStatement(sql));
            }
        }
    }

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

        try {
            PreparedStatement preparedStatement = isMNV 
                    ? preparedStatement4MNVGene : preparedStatement4VariantGeneMap.get(chr);
            preparedStatement.setInt(1, pos);
            preparedStatement.setString(2, alt);
            preparedStatement.setString(3, gene);
            ResultSet rs = preparedStatement.executeQuery();
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

    public static float getScore(CSVRecord record) {
        return FormatManager.getFloat(record, getHeader());
    }

    public static ArrayList<Trap> getTrapList(String chr, int pos, String alt) {
        ArrayList<Trap> list = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = preparedStatement4VariantMap.get(chr);
            preparedStatement.setInt(1, pos);
            preparedStatement.setString(2, alt);
            ResultSet rs = preparedStatement.executeQuery();
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
