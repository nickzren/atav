package function.coverage.base;

import function.genotype.base.GenotypeLevelFilterCommand;
import function.variant.base.Region;
import global.Data;
import utils.DBManager;
import utils.ErrorManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class DBUtils {

    public static void updateGeneCoverageSummary(String gene, HashMap<Integer, Integer> sample_ids, int[] coverage_ratio) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("Insert ignore into gene_coverage_summary (gene,sample_id,min_coverage,coverage_ratio) Values ");
        try {
            int count = 0;
            for (int i = 0; i < coverage_ratio.length; i++) {
                if (coverage_ratio[i] >= 0) {
                    if (count > 0) {
                        sb.append(",");
                    }
                    sb.append("(").append("'").append(gene).append("',").append(sample_ids.get(i)).append(",");
                    sb.append(GenotypeLevelFilterCommand.minCoverage).append(",").append(coverage_ratio[i]).append(")");
                    count++;

                }
            }
            if (count > 0) {
                DBManager.executeUpdate(sb.toString());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }

    }

    //check if there is at least one record for the query
    public static boolean isEmpty(String strQuery) throws SQLException {
        ResultSet rset = DBManager.executeQuery(strQuery);
        boolean result = rset.next();
        rset.close();
        return result;
    }

    //check if there is at least one record for the query
    public static int getUniqueInt(String strQuery, String field) throws SQLException {
        ResultSet rset = DBManager.executeQuery(strQuery);
        int result = Data.NA;
        if (rset.next()) {
            result = rset.getInt(field);
        }
        rset.close();
        return result;
    }

    public static ArrayList<Integer> getIntList(String strQuery, String field) throws SQLException {
        ResultSet rset = DBManager.executeQuery(strQuery);
        ArrayList<Integer> result = new ArrayList<Integer>();
        while (rset.next()) {
            result.add(rset.getInt(field));
        }
        rset.close();
        return result;
    }

    public static Region getTranslatedRegion(String strQuery) {
        if (!strQuery.isEmpty()) {
            ResultSet rs = null;
            try {
                rs = DBManager.executeQuery(strQuery);
                int seq_region_start = 0;
                int seq_region_end = 0;
                int valid_seq_region_id = 0;

                if (rs.next()) {
                    do {
                        int seq_region_id = rs.getInt("seq_region_id");
                        if (seq_region_id >= 27504 && seq_region_id <= 27527) { //temp fix for bug 479
                            if (seq_region_start == 0) {
                                seq_region_start = rs.getInt("pos");
                            } else {
                                seq_region_end = rs.getInt("pos");
                            }
                            valid_seq_region_id = seq_region_id;
                        }
                    } while (rs.next());
                }
                rs.close();

                if (seq_region_end > seq_region_start) {
                    String chr = getChrByRegionId(valid_seq_region_id);
                    return new Region(valid_seq_region_id, chr, seq_region_start, seq_region_end);
                }

            } catch (Exception e) {
                ErrorManager.send(e);
            }
        }
        return null;
    }

    public static String getChrByRegionId(int id) throws Exception {
        String sql = "SELECT name FROM seq_region where coord_system_id = 2 AND "
                + "seq_region_id = " + id;

        ResultSet rset = DBManager.executeQuery(sql);

        if (rset.next()) {
            return rset.getString("name");
        }

        rset.close();

        return "";
    }

    public static InputList getExonList(String strQuery) {
        InputList ExonList = new InputList();
        if (!strQuery.isEmpty()) {
            ResultSet rs = null;
            try {
                rs = DBManager.executeQuery(strQuery);
                if (rs.next()) {
                    do {
                        int exon_id = rs.getInt("exon_id");
                        int seq_region_id = rs.getInt("seq_region_id");
                        int seq_region_start = rs.getInt("seq_region_start");
                        int seq_region_end = rs.getInt("seq_region_end");
                        String stable_id = rs.getString("stable_id");
                        String chr = rs.getString("name");
                        String trans_stable_id = rs.getString("t_stable_id");
                        try {
                            ExonList.add(new Exon(exon_id, stable_id, seq_region_id, chr, seq_region_start, seq_region_end, trans_stable_id));
                        } catch (Exception e) {
                            //do nothing for now, we just had an exon with non chromosome region
                        }
                    } while (rs.next());
                }

                rs.close();
            } catch (Exception e) {
                ErrorManager.send(e);
            }
        }
        return ExonList;
    }
}
