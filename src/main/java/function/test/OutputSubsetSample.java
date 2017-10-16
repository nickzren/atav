package function.test;

import function.variant.base.RegionManager;
import java.sql.SQLException;
import utils.DBManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class OutputSubsetSample {

    // minor config tweak for this task
    // CommonCommand.isNonSampleAnalysis = true;
    public static final String OUTPUT_PATH = "/nfs/seqscratch_ssd/zr2180/waldb/";

    public static void run() throws SQLException {
        String sql = "CREATE TEMPORARY TABLE IF NOT EXISTS exome_sample_id AS "
                + "(SELECT sample_id FROM sample where sample_type = 'exome' "
                + "and sample_name not like '%SRR%' "
                + "and sample_finished = 1 "
                + "and sample_failure = 0 limit 10000)";
        updateSQL(sql);

//        outputCarrierData();

//        outputNonCarrierData();
    }

    public static void outputCarrierData() throws SQLException {
        for (String chr : RegionManager.ALL_CHR) {
            // get variant id , pos, ref, alt data
            String sql = "select distinct variant_id,POS,REF,ALT from WalDB.variant_chr" + chr
                    + " into outfile '" + OUTPUT_PATH + "variant_pos_chr" + chr + ".txt';";
            executeSQL(sql);
//
//            // create variant id table            
            sql = "CREATE temporary TABLE WalDB.variant_pos_chr" + chr + " ("
                    + " variant_id int(10) unsigned NOT NULL,"
                    + " POS int(10) unsigned NOT NULL,"
                    + " REF varchar(255) NOT NULL,"
                    + " ALT varchar(255) NOT NULL,"
                    + " PRIMARY KEY (variant_id)"
                    + ") ENGINE=TokuDB;";
            updateSQL(sql);
//
//            // load variant id, pos, ref, alt into table
            sql = "load data infile '" + OUTPUT_PATH + "variant_pos_chr" + chr + ".txt' "
                    + "ignore into table WalDB.variant_pos_chr" + chr;
            executeSQL(sql);
//
//            // get carrier data
            sql = "select CONCAT('" + chr + "','-',c.block_id),c.sample_id,'" + chr + "',v.POS,v.REF,v.ALT"
                    + ",c.GT,c.DP,c.AD_REF,c.AD_ALT,c.GQ,c.VQSLOD,c.FS,c.MQ,c.QD,c.QUAL"
                    + ",c.ReadPosRankSum,c.MQRankSum,c.FILTER "
                    + "from WalDB.variant_pos_chr" + chr + " v"
                    + ", WalDB.called_variant_chr" + chr + " c"
                    + ", exome_sample_id s "
                    + "where v.variant_id = c.variant_id and c.sample_id = s.sample_id "
                    + "into outfile '" + OUTPUT_PATH + "called_variant_chr" + chr + ".txt';";
            executeSQL(sql);

            sql = "drop table WalDB.variant_pos_chr" + chr;
            updateSQL(sql);
        }
    }

    public static void outputNonCarrierData() throws SQLException {
        for (String chr : RegionManager.ALL_CHR) {
            String sql = "select CONCAT('" + chr + "','-',d.block_id),d.sample_id,d.DP_string from DP_bins_chr" + chr + " d, exome_sample_id s "
                    + "where d.sample_id = s.sample_id "
                    + "into outfile '" + OUTPUT_PATH + "DP_bins_chr" + chr + ".txt';";
            executeSQL(sql);
        }
    }

    public static void executeSQL(String sql) throws SQLException {
        LogManager.writeAndPrint(sql);
        DBManager.executeQuery(sql);
    }

    public static void updateSQL(String sql) throws SQLException {
        LogManager.writeAndPrint(sql);
        DBManager.executeUpdate(sql);
    }
}
