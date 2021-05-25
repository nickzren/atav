package function.test;

import function.variant.base.RegionManager;
import java.sql.SQLException;
import utils.DBManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class OutputSubsetSample {

    // minor config tweak for this task
    // CommonCommand.isNonSampleAnalysis = true;
    public static final String OUTPUT_PATH = "/nfs/informatics/data/tmp/waldb_120720/";

    public static void run() throws SQLException {
        String sql = "CREATE TEMPORARY TABLE IF NOT EXISTS exome_sample_id AS "
                + "(SELECT sample_id FROM sample where sample_id in (111242))";
        updateSQL(sql);

        sql = "SELECT * FROM sample where "
                + "sample_id in (111242) "
                + "into outfile '" + OUTPUT_PATH + "sample';";
        executeSQL(sql);
        
        sql = "SELECT sample_id,sample_id,0,0,1,1,sample_type,capture_kit FROM sample where "
                + " sample_id in (111242) "
                + "into outfile '" + OUTPUT_PATH + "atav_sample.txt';";
        executeSQL(sql);

        sql = "SELECT * FROM hgnc "
                + "into outfile '" + OUTPUT_PATH + "hgnc';";
        executeSQL(sql);
        
        sql = "SELECT * FROM effect_ranking "
                + "into outfile '" + OUTPUT_PATH + "effect_ranking';";
        executeSQL(sql);
        
        sql = "SELECT * FROM codingandsplice_effect "
                + "into outfile '" + OUTPUT_PATH + "codingandsplice_effect';";
        executeSQL(sql);
        
        sql = "SELECT * FROM full_impact "
                + "into outfile '" + OUTPUT_PATH + "full_impact';";
        executeSQL(sql);
        
        sql = "SELECT * FROM low_impact "
                + "into outfile '" + OUTPUT_PATH + "low_impact';";
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
                    + ") ENGINE=MEMORY;";
            updateSQL(sql);

            // load variant id into table
            sql = "load data infile '" + OUTPUT_PATH + "variant_id_chr" + chr + ".txt' "
                    + "ignore into table WalDB.variant_id_chr" + chr;
            executeSQL(sql);
//            String[] cmd = {"rm " + OUTPUT_PATH + "variant_id_chr" + chr + ".txt "};
//            ThirdPartyToolManager.systemCall(cmd);

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
                    + " into outfile '" + OUTPUT_PATH + "variant_chr" + chr + "';";
            executeSQL(sql);
//            cmd[0] = "gzip -9 " + OUTPUT_PATH + "variant_chr" + chr;
//            ThirdPartyToolManager.systemCall(cmd);
            
            // create variant pos table            
            sql = "CREATE temporary TABLE WalDB.variant_pos_chr" + chr + " ("
                    + " variant_id int(10) unsigned NOT NULL,"
                    + " POS int(10) unsigned NOT NULL,"
                    + " REF varchar(255) NOT NULL,"
                    + " ALT varchar(255) NOT NULL,"
                    + " PRIMARY KEY (variant_id)"
                    + ") ENGINE=MEMORY;";
            updateSQL(sql);
//
//            // load variant id, pos, ref, alt into table
            sql = "load data infile '" + OUTPUT_PATH + "variant_pos_chr" + chr + ".txt' "
                    + "ignore into table WalDB.variant_pos_chr" + chr;
            executeSQL(sql);
//            cmd[0] = "rm " + OUTPUT_PATH + "variant_pos_chr" + chr + ".txt ";
//            ThirdPartyToolManager.systemCall(cmd);

//            // get carrier data
            sql = "select c.* "
                    + "from WalDB.variant_pos_chr" + chr + " v"
                    + ", WalDB.called_variant_chr" + chr + " c"
                    + ", exome_sample_id s "
                    + "where v.variant_id = c.variant_id and c.sample_id = s.sample_id "
                    + "into outfile '" + OUTPUT_PATH + "called_variant_chr" + chr + "';";
            executeSQL(sql);
//            cmd[0] = "gzip -9 " + OUTPUT_PATH + "called_variant_chr" + chr;
//            ThirdPartyToolManager.systemCall(cmd);

            sql = "drop table WalDB.variant_pos_chr" + chr;
            updateSQL(sql);
        }
    }

    public static void outputNonCarrierData() throws SQLException {
        for (String chr : RegionManager.ALL_CHR) {
            String sql = "select d.* from DP_bins_chr" + chr + " d, exome_sample_id s "
                    + "where d.sample_id = s.sample_id "
                    + "into outfile '" + OUTPUT_PATH + "DP_bins_chr" + chr + "';";
            executeSQL(sql);
//            String[] cmd = {"gzip -9 " + OUTPUT_PATH + "DP_bins_chr" + chr};
//            ThirdPartyToolManager.systemCall(cmd);
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
