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
//        for (String chr : RegionManager.ALL_CHR) {
//            String sql = "select * from WalDB.called_variant_chr" + chr + " "
//                    + "into outfile '/nfs/seqscratch_ssd/zr2180/WalDB/called_variant/called_variant_chr" + chr + "_old.txt';";
//            executeSQL(sql);
//            
//            // get variant id , pos data
//            sql = "select distinct variant_id,POS from WalDB.variant_chr" + chr
//                    + " into outfile '/nfs/seqscratch_ssd/zr2180/WalDB/variant_pos/variant_pos_chr" + chr + ".txt';";
//            executeSQL(sql);
//
//            // get variant id , pos table            
//            sql = "CREATE TABLE WalDB.variant_pos_chr" + chr + " ("
//                    + " variant_id int(10) unsigned NOT NULL,"
//                    + " POS int(10) unsigned NOT NULL,"
//                    + " PRIMARY KEY (variant_id)"
//                    + ") ENGINE=TokuDB;";
//            updateSQL(sql);
//
//            // load variant id, pos into table
//            sql = "load data infile '/nfs/seqscratch_ssd/zr2180/WalDB/variant_pos/variant_pos_chr" + chr + ".txt' "
//                    + "into table WalDB.variant_pos_chr" + chr;
//            executeSQL(sql);
//
//            // get 1k block carrier data
//            sql = "select c.sample_id,c.variant_id,floor(v.POS / 1000)"
//                    + ",c.GT,c.DP,c.AD_REF,c.AD_ALT,c.GQ,c.VQSLOD,c.SOR,c.FS,c.MQ,c.QD,c.QUAL"
//                    + ",c.ReadPosRankSum,c.MQRankSum,c.FILTER,c.highest_impact,c.PID_variant_id,"
//                    + "c.PGT,c.HP_variant_id,c.HP_GT,c.PQ "
//                    + "from WalDB.variant_pos_chr" + chr + " v, WalDB.called_variant_chr" + chr + " c "
//                    + "where v.variant_id = c.variant_id into outfile '/nfs/seqscratch_ssd/zr2180/WalDB/called_variant/called_variant_chr" + chr + ".txt';";
//            executeSQL(sql);
//
//            // drop current carrier table
//            sql = "truncate TABLE WalDB.called_variant_chr" + chr;
//            updateSQL(sql);
//
//            // load carrier data into table
//            sql = "load data infile '/nfs/seqscratch_ssd/zr2180/WalDB/called_variant/called_variant_chr" + chr + ".txt' "
//                    + "into table WalDB.called_variant_chr" + chr;
//            executeSQL(sql);
//            
//            sql = "drop table WalDB.variant_pos_chr" + chr;
//            updateSQL(sql);
//        }
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
