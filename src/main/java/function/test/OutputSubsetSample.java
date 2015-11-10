package function.test;

import global.Data;
import java.sql.SQLException;
import utils.DBManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class OutputSubsetSample {

    public static final String OUTPUT_PATH = "/nfs/seqscratch10/ANNOTATION/tmp/annodb_pgm/";

    public static void run() throws SQLException {
        outputCarrierData();

        outputNonCarrierData();
    }

    public static void outputCarrierData() throws SQLException {
        String snvCarrierSql = "SELECT * "
                + "FROM called_snv va,"
                + Data.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE va.sample_id = t.id "
                + "INTO OUTFILE '" + OUTPUT_PATH + "called_snv.txt'";

        LogManager.writeAndPrint(snvCarrierSql);
        DBManager.executeQuery(snvCarrierSql);

        String indelCarrierSql = "SELECT * "
                + "FROM called_indel va,"
                + Data.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE va.sample_id = t.id "
                + "INTO OUTFILE '" + OUTPUT_PATH + "called_indel.txt'";

        LogManager.writeAndPrint(indelCarrierSql);
        DBManager.executeQuery(indelCarrierSql);
    }

    public static void outputNonCarrierData() throws SQLException {
        for (int i = 0; i < Data.SAMPLE_TYPE.length; i++) {
            for (String chr : Data.ALL_CHR) {
                String nonCarrierSql = "SELECT * "
                        + "FROM " + Data.SAMPLE_TYPE[i]
                        + "_read_coverage_" + Data.COVERAGE_BLOCK_SIZE + "_chr" + chr + " c,"
                        + Data.SAMPLE_TYPE[i] + "_sample_id t "
                        + "WHERE c.sample_id = t.id "
                        + "INTO OUTFILE '" + OUTPUT_PATH + Data.SAMPLE_TYPE[i]
                        + "_read_coverage_" + Data.COVERAGE_BLOCK_SIZE + "_chr" + chr + ".txt'";

                LogManager.writeAndPrint(nonCarrierSql);
                DBManager.executeQuery(nonCarrierSql);
            }
        }
    }
}
