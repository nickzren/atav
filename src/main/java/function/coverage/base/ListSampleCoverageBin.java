package function.coverage.base;

import function.AnalysisBase;
import function.annotation.base.Gene;
import function.annotation.base.GeneManager;
import static function.genotype.base.CoverageBlockManager.COVERAGE_BLOCK_SIZE;
import function.genotype.base.SampleManager;
import function.variant.base.Region;
import global.Data;
import global.Index;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import utils.CommonCommand;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListSampleCoverageBin extends AnalysisBase {

    BufferedWriter bwCoverageBin = null;
    final String coverageBinFilePath = CommonCommand.outputPath + "sample.coverage.bin.csv";

    @Override
    public void initOutput() {
        try {
            bwCoverageBin = new BufferedWriter(new FileWriter(coverageBinFilePath));
            bwCoverageBin.write("Sample,Gene,Chr,Block End Position,Samtools Raw Coverage");
            bwCoverageBin.newLine();

        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwCoverageBin.flush();
            bwCoverageBin.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {

    }

    @Override
    public void beforeProcessDatabaseData() {

    }

    @Override
    public void afterProcessDatabaseData() {

    }

    @Override
    public void processDatabaseData() {
        try {
            for (String geneName : GeneManager.getMap().keySet()) {
                count(geneName);

                String chr = null;
                int start = Data.NA;
                int end = Data.NA;

                String sql = "select chrom,start,end from hgnc where gene = '" + geneName + "'";

                ResultSet rs = DBManager.executeQuery(sql);

                if (rs.next()) {
                    chr = rs.getString("chrom");
                    start = rs.getInt("start");
                    end = rs.getInt("end");
                }

                if (chr != null) {
                    int startBlockEndPos = getBlockEndPos(start);
                    int endBlockEndPos = getBlockEndPos(end);

                    outputCovBin(Index.GENOME, chr, geneName, startBlockEndPos, endBlockEndPos);
                    outputCovBin(Index.EXOME, chr, geneName, startBlockEndPos, endBlockEndPos);
                }
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void outputCovBin(int sampleTypeIndex, String chr, String geneName,
            int startBlockEndPos, int endBlockEndPos) throws Exception {
        for (int endBlockPos = startBlockEndPos; endBlockPos <= endBlockEndPos; endBlockPos += 1024) {
            if (!GeneManager.getGeneBoundaryList().isEmpty()) {
                boolean isValid = false;

                for (Gene gene : GeneManager.getMap().get(geneName)) {
                    Region r = new Region(chr, endBlockPos, endBlockPos);

                    if (gene.contains(r)) {
                        isValid = true;
                        break;
                    }
                }

                if (!isValid) {
                    continue;
                }
            }

            String sql = "SELECT sample_id, min_coverage "
                    + "FROM " + SampleManager.SAMPLE_TYPE[sampleTypeIndex] + "_read_coverage_1024_chr" + chr + " c, "
                    + SampleManager.ALL_SAMPLE_ID_TABLE + " t WHERE c.position = " + endBlockPos + ""
                    + " AND c.sample_id = t.id";

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                int sampleId = rs.getInt("sample_id");
                String covBin = rs.getString("min_coverage");

                StringBuilder sb = new StringBuilder();

                sb.append(SampleManager.getMap().get(sampleId).getName()).append(",");
                sb.append(geneName).append(",");
                sb.append(chr).append(",");
                sb.append(endBlockPos).append(",");
                sb.append(covBin.replaceAll(",", ""));

                bwCoverageBin.write(sb.toString());
                bwCoverageBin.newLine();
            }
        }
    }

    private static int getBlockEndPos(int position) {
        int posIndex = position % COVERAGE_BLOCK_SIZE;

        if (posIndex == 0) {
            posIndex = COVERAGE_BLOCK_SIZE; // block boundary is ( ] 
        }

        return position - posIndex + COVERAGE_BLOCK_SIZE;
    }

    private void count(String gene) {
        System.out.print("Processing " + gene + "                              \r");
    }

    @Override
    public String toString() {
        return "Start running list sample coverage bin function";
    }
}
