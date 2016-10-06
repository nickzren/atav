package function.coverage.summary;

import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageAnalysisBase;
import function.coverage.base.CoverageCommand;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author qwang, nick
 */
public class CoverageSummary extends CoverageAnalysisBase {

    public BufferedWriter bwCoverageDetailsByExon = null;
    public final String coverageDetailsByExonFilePath = CommonCommand.outputPath + "coverage.details.by.exon.csv";

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwCoverageDetailsByExon = new BufferedWriter(new FileWriter(coverageDetailsByExonFilePath));
            bwCoverageDetailsByExon.write("Sample,Gene,Chr,Exon,Start_Position, Stop_Position,Length,Covered_Base,%Bases_Covered,Coverage_Status");
            bwCoverageDetailsByExon.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            super.closeOutput();

            bwCoverageDetailsByExon.flush();
            bwCoverageDetailsByExon.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void processExon(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        outputCoverageDetailsByExon(sampleCoveredLengthMap, gene, exon);
    }

    private void outputCoverageDetailsByExon(HashMap<Integer, Integer> sampleCoveredLengthMap,
            Gene gene, Exon exon) {
        try {
            StringBuilder sb = new StringBuilder();
            for (Sample sample : SampleManager.getList()) {
                sb.append(sample.getName()).append(",");
                sb.append(gene.getName()).append(",");
                sb.append(exon.getChrStr()).append(",");
                sb.append(exon.getIdStr()).append(",");
                sb.append(exon.getStartPosition()).append(",");
                sb.append(exon.getEndPosition()).append(",");
                sb.append(exon.getLength()).append(",");

                Integer coveredLength = sampleCoveredLengthMap.get(sample.getId());
                if (coveredLength == null) {
                    coveredLength = 0;
                }
                sb.append(coveredLength).append(",");

                double ratio = MathManager.devide(coveredLength, exon.getLength());
                sb.append(FormatManager.getSixDegitDouble(ratio)).append(",");
                sb.append(ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0);

                writeToFile(sb.toString(), bwCoverageDetailsByExon);
                sb.setLength(0);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running coverage summary function";
    }
}
