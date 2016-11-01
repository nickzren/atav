package function.test;

import function.genotype.base.DPBinBlockManager;
import function.genotype.base.SampleManager;
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
    // server annodb04
    public static final String OUTPUT_PATH = "/nfs/seqscratch11/zr2180/";

    public static void run() throws SQLException {
        outputCarrierData();

        outputNonCarrierData();
    }

    public static void outputCarrierData() throws SQLException {   
        String snvCarrierSql = "SELECT * "
                + "FROM called_snv va,"
                + SampleManager.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE va.sample_id = t.id "
                + "INTO OUTFILE '" + OUTPUT_PATH + "called_snv.txt'";

        LogManager.writeAndPrint(snvCarrierSql);
        DBManager.executeQuery(snvCarrierSql);

        String indelCarrierSql = "SELECT * "
                + "FROM called_indel va,"
                + SampleManager.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE va.sample_id = t.id "
                + "INTO OUTFILE '" + OUTPUT_PATH + "called_indel.txt'";

        LogManager.writeAndPrint(indelCarrierSql);
        DBManager.executeQuery(indelCarrierSql);
    }

    public static void outputNonCarrierData() throws SQLException {
        for (int i = 0; i < SampleManager.SAMPLE_TYPE.length; i++) {
            for (String chr : RegionManager.ALL_CHR) {
                String nonCarrierSql = "SELECT * "
                        + "FROM " + SampleManager.SAMPLE_TYPE[i]
                        + "_read_coverage_" + DPBinBlockManager.DP_BIN_BLOCK_SIZE + "_chr" + chr + " c,"
                        + SampleManager.SAMPLE_TYPE[i] + "_sample_id t "
                        + "WHERE c.sample_id = t.id "
                        + "INTO OUTFILE '" + OUTPUT_PATH + SampleManager.SAMPLE_TYPE[i]
                        + "_read_coverage_" + DPBinBlockManager.DP_BIN_BLOCK_SIZE + "_chr" + chr + ".txt'";

                LogManager.writeAndPrint(nonCarrierSql);
                DBManager.executeQuery(nonCarrierSql);
            }
        }
    }
}
