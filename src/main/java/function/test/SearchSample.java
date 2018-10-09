package function.test;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import function.variant.base.RegionManager;
import java.sql.ResultSet;
import java.util.HashSet;
import utils.DBManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class SearchSample {

    public static void run() throws Exception {
        HashSet<Integer> sampleIdSet = new HashSet<>();

        for (Sample sample : SampleManager.getList()) {
            for (String chr : RegionManager.ALL_CHR) {
                String table = "called_variant_chr" + chr;
                int count = getCount(table, sample);
                if (count == 0) {
                    sampleIdSet.add(sample.getId());
                    break;
                }

                table = "DP_bins_chr" + chr;
                count = getCount(table, sample);
                
                if (count == 0) {
                    sampleIdSet.add(sample.getId());
                    break;
                }
            }
        }

        for (Integer id : sampleIdSet) {
            LogManager.writeAndPrintNoNewLine(id.toString());
        }
    }

    private static int getCount(String table, Sample sample) throws Exception {
        String sql = "select count(*) count from " + table + " "
                + "where sample_id = " + sample.getId();

        ResultSet rset = DBManager.executeQuery(sql);

        if (rset.next()) {
            return rset.getInt("count");
        }

        return 0;
    }
}
