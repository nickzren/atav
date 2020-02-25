package function.coverage.summary;

import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.coverage.base.CoverageAnalysisBase;
import function.coverage.base.CoverageCommand;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author qwang, nick
 */
public class CoverageSummary extends CoverageAnalysisBase {

    public BufferedWriter bwCoverageDetailByExon = null;
    public final String coverageDetailByExonFilePath = CommonCommand.outputPath + "coverage.detail.by.exon.csv";

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwCoverageDetailByExon = new BufferedWriter(new FileWriter(coverageDetailByExonFilePath));
            bwCoverageDetailByExon.write("Sample,Gene,ExonID,Chr,Start_Position,Stop_Position,Length,Covered_Base,%Bases_Covered,Coverage_Status");
            bwCoverageDetailByExon.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            super.closeOutput();

            bwCoverageDetailByExon.flush();
            bwCoverageDetailByExon.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void processExon(HashMap<Integer, Integer> sampleCoveredLengthMap, Gene gene, Exon exon) {
        outputCoverageDetailByExon(sampleCoveredLengthMap, gene, exon);
    }

    private void outputCoverageDetailByExon(HashMap<Integer, Integer> sampleCoveredLengthMap,
            Gene gene, Exon exon) {
        try {
            for (Sample sample : SampleManager.getList()) {
                StringJoiner sj = new StringJoiner(",");
                sj.add(sample.getName());
                sj.add(gene.getName());
                sj.add(FormatManager.getInteger(exon.getId()));
                sj.add(exon.getChrStr());
                sj.add(FormatManager.getInteger(exon.getStartPosition()));
                sj.add(FormatManager.getInteger(exon.getEndPosition()));
                sj.add(FormatManager.getInteger(exon.getLength()));

                Integer coveredLength = sampleCoveredLengthMap.get(sample.getId());
                if (coveredLength == null) {
                    coveredLength = 0;
                }
                sj.add(FormatManager.getInteger(coveredLength));

                double ratio = MathManager.devide(coveredLength, exon.getLength());
                sj.add(FormatManager.getDouble(ratio));
                sj.add(FormatManager.getInteger(ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0));

                writeToFile(sj.toString(), bwCoverageDetailByExon);
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
