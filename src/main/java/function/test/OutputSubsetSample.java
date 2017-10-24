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
    public static final String OUTPUT_PATH = "/nfs/seqscratch_ssd/zr2180/waldb_chrY/";

    public static void run() throws SQLException {
        String sql = "CREATE TEMPORARY TABLE IF NOT EXISTS exome_sample_id AS "
                + "(SELECT sample_id FROM sample where sample_type = 'exome' "
                + "and sample_name not like '%SRR%' "
                + "and sample_finished = 1 "
                + "and sample_failure = 0 limit 10000)";
        updateSQL(sql);
        
        sql = "SELECT sample_id,sample_id,sample_type,capture_kit,prep_id,NULL,0,1,0 FROM sample where sample_type = 'exome' "
                + "and sample_name not like '%SRR%' "
                + "and sample_finished = 1 "
                + "and sample_failure = 0 limit 10000 "
                + "into outfile '" + OUTPUT_PATH + "db_load_10k_exome_sample.txt';";
        executeSQL(sql);

        sql = "SELECT sample_id,sample_id,0,0,1,1,sample_type,capture_kit FROM sample where sample_type = 'exome' "
                + "and sample_name not like '%SRR%' "
                + "and sample_finished = 1 "
                + "and sample_failure = 0 limit 10000 "
                + "into outfile '" + OUTPUT_PATH + "10k_exome_atav_sample.txt';";
        executeSQL(sql);

        outputCarrierData();
        outputNonCarrierData();
    }

    public static void outputCarrierData() throws SQLException {
        for (String chr : RegionManager.ALL_CHR) {
            // select all variant id based on exome samples
            String sql = "select distinct c.variant_id from "
                    + "WalDB.called_variant_chr" + chr + " c"
                    + ", exome_sample_id s "
                    + "where c.sample_id = s.sample_id "
                    + "into outfile '" + OUTPUT_PATH + "variant_id_chr" + chr + ".txt';";
            executeSQL(sql);

            // create temp variant id table
            sql = "CREATE temporary TABLE WalDB.variant_id_chr" + chr + " ("
                    + " variant_id int(10) unsigned NOT NULL,"
                    + " PRIMARY KEY (variant_id)"
                    + ") ENGINE=TokuDB;";
            updateSQL(sql);

            // load variant id into table
            sql = "load data infile '" + OUTPUT_PATH + "variant_id_chr" + chr + ".txt' "
                    + "ignore into table WalDB.variant_id_chr" + chr;
            executeSQL(sql);

            // get variant id , pos, ref, alt data
            sql = "select distinct v.variant_id,v.POS,v.REF,v.ALT from WalDB.variant_chr" + chr
                    + " v, WalDB.variant_id_chr" + chr + " i "
                    + " where v.variant_id = i.variant_id"
                    + " into outfile '" + OUTPUT_PATH + "variant_pos_chr" + chr + ".txt';";
            executeSQL(sql);

            // get all variant level data
            sql = "select v.* from WalDB.variant_chr" + chr
                    + " v, WalDB.variant_id_chr" + chr + " i "
                    + " where v.variant_id = i.variant_id"
                    + " into outfile '" + OUTPUT_PATH + "db_load_variant_chr" + chr + ".txt';";
            executeSQL(sql);

            // create variant pos table            
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
            sql = "select c.* "
                    + "from WalDB.variant_pos_chr" + chr + " v"
                    + ", WalDB.called_variant_chr" + chr + " c"
                    + ", exome_sample_id s "
                    + "where v.variant_id = c.variant_id and c.sample_id = s.sample_id "
                    + "into outfile '" + OUTPUT_PATH + "db_load_called_variant_chr" + chr + ".txt';";
            executeSQL(sql);

            sql = "drop table WalDB.variant_pos_chr" + chr;
            updateSQL(sql);
        }
    }

    public static void outputNonCarrierData() throws SQLException {
        for (String chr : RegionManager.ALL_CHR) {
            String sql = "select d.* from DP_bins_chr" + chr + " d, exome_sample_id s "
                    + "where d.sample_id = s.sample_id "
                    + "into outfile '" + OUTPUT_PATH + "db_load_DP_bins_chr" + chr + ".txt';";
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
