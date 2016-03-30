package function.test;

import function.genotype.base.CoverageBlockManager;
import function.genotype.base.SampleManager;
import function.variant.base.RegionManager;
import global.Data;
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
    public static final String OUTPUT_PATH = "/nfs/seqscratch10/ANNOTATION/tmp/annodb_pgm/";

    public static void run() throws SQLException {
//        outputCarrierData();
//
//        outputNonCarrierData();

        outputGeneCoverageSummary();
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
                        + "_read_coverage_" + CoverageBlockManager.COVERAGE_BLOCK_SIZE + "_chr" + chr + " c,"
                        + SampleManager.SAMPLE_TYPE[i] + "_sample_id t "
                        + "WHERE c.sample_id = t.id "
                        + "INTO OUTFILE '" + OUTPUT_PATH + SampleManager.SAMPLE_TYPE[i]
                        + "_read_coverage_" + CoverageBlockManager.COVERAGE_BLOCK_SIZE + "_chr" + chr + ".txt'";

                LogManager.writeAndPrint(nonCarrierSql);
                DBManager.executeQuery(nonCarrierSql);
            }
        }
    }

    public static void outputGeneCoverageSummary() throws SQLException {
        String geneCoverageSummarySql = "SELECT * "
                + "FROM gene_coverage_summary g,"
                + SampleManager.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE g.sample_id = t.id "
                + "INTO OUTFILE '" + OUTPUT_PATH + "gene_coverage_summary_subset.txt'";

        LogManager.writeAndPrint(geneCoverageSummarySql);
        DBManager.executeQuery(geneCoverageSummarySql);
    }
}
