package function.external.evs;

import function.external.base.DataManager;
import function.variant.base.Region;
import java.sql.PreparedStatement;
import java.util.StringJoiner;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class EvsManager {

    static final String table = "evs.variant_2015_09_16";

    private static PreparedStatement preparedStatement4Variant;
    private static PreparedStatement preparedStatement4Region;
    
    public static void init() {
        if (EvsCommand.isInclude) {
            String sql = "SELECT all_maf,all_genotype_count,FilterStatus FROM " 
                    + table + " WHERE chr=? AND position=? AND ref_allele=? AND alt_allele=?";
            preparedStatement4Variant = DBManager.initPreparedStatement(sql);

            sql = "SELECT chr,position,ref_allele,alt_allele,"
                + "all_maf,all_genotype_count,FilterStatus FROM " 
                    + table + " WHERE chr=? AND position BETWEEN ? AND ?";
            preparedStatement4Region = DBManager.initPreparedStatement(sql);
        }
    }
    
    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Evs All Maf");
        sj.add("Evs All Genotype Count");
        sj.add("Evs Filter Status");

        return sj.toString();
    }

    public static String getVersion() {
        return "EVS: " + DataManager.getVersion(table) + "\n";
    }
    
    public static PreparedStatement getPreparedStatement4Variant() {
        return preparedStatement4Variant;
    }

    public static PreparedStatement getPreparedStatement4Region() {
        return preparedStatement4Region;
    }
}
