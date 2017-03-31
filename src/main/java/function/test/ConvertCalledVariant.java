package function.test;

import function.variant.base.RegionManager;
import java.sql.SQLException;
import utils.DBManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class ConvertCalledVariant {

    public static void run() throws SQLException {
        for (String chr : RegionManager.ALL_CHR) {
            // get variant id , pos data
            String sql = "select distinct variant_id,POS from WalDB.variant_chr" + chr
                    + " into outfile '/nfs/seqscratch_ssd/zr2180/WalDB/called_variant/variant_pos_chr" + chr + ".txt';";
            executeSQL(sql);

            // get variant id , pos table            
            sql = "CREATE TABLE WalDB.variant_pos_chr" + chr + " ("
                    + " variant_id int(10) unsigned NOT NULL,"
                    + " POS int(10) unsigned NOT NULL,"
                    + " PRIMARY KEY (variant_id)"
                    + ") ENGINE=TokuDB;";
            updateSQL(sql);

            // load variant id, pos into table
            sql = "load data infile '/nfs/seqscratch_ssd/zr2180/WalDB/called_variant/variant_pos_chr" + chr + ".txt' "
                    + "into table WalDB.variant_pos_chr" + chr;
            executeSQL(sql);

            // get 1k block carrier data
            sql = "select c.sample_id,c.variant_id,floor(v.POS / 1000)"
                    + ",c.GT,c.DP,c.AD_REF,c.AD_ALT,c.GQ,c.VQSLOD,c.FS,c.MQ,c.QD,c.QUAL"
                    + ",c.ReadPosRankSum,c.MQRankSum,c.FILTER "
                    + "from WalDB.variant_pos_chr" + chr + " v, WalDB.called_variant_chr" + chr + " c "
                    + "where v.variant_id = c.variant_id into outfile '/nfs/seqscratch_ssd/zr2180/WalDB/called_variant/called_variant_chr" + chr + ".txt';";
            executeSQL(sql);

            /*
            // drop current carrier table
            sql = "DROP TABLE WalDB.called_variant_chr" + chr;
            updateSQL(sql);

            // create carrier table
            sql = "CREATE TABLE WalDB.called_variant_chr" + chr + " ("
                    + " sample_id mediumint(8) unsigned NOT NULL,"
                    + " variant_id int(10) unsigned NOT NULL,"
                    + " block_id mediumint(8) unsigned NOT NULL,"
                    + " GT tinyint(3) unsigned NOT NULL,"
                    + " DP smallint(5) unsigned DEFAULT NULL,"
                    + " AD_REF smallint(6) DEFAULT NULL,"
                    + " AD_ALT smallint(5) unsigned DEFAULT NULL,"
                    + " GQ tinyint(3) unsigned NOT NULL,"
                    + " VQSLOD float DEFAULT NULL,"
                    + " FS float DEFAULT NULL,"
                    + " MQ tinyint(3) unsigned DEFAULT NULL,"
                    + " QD tinyint(3) unsigned DEFAULT NULL,"
                    + " QUAL mediumint(8) unsigned DEFAULT NULL,"
                    + " ReadPosRankSum float DEFAULT NULL,"
                    + " MQRankSum float DEFAULT NULL,"
                    + " FILTER enum('PASS','LIKELY','INTERMEDIATE','FAIL') DEFAULT NULL,"
                    + " PRIMARY KEY (block_id,sample_id,variant_id),"
                    + " KEY variant_idx (variant_id),"
                    + " KEY sample_idx (sample_id,variant_id)"
                    + ") ENGINE=TokuDB;";
            updateSQL(sql);

            // load carrier data into table
            sql = "load data infile '/nfs/seqscratch_ssd/zr2180/WalDB/called_variant/called_variant_chr" + chr + ".txt' "
                    + "into table WalDB.called_variant_chr" + chr;
            executeSQL(sql);
             */
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
