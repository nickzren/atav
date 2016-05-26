package function.coverage.summary;

import function.coverage.base.CoverageManager;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import utils.FormatManager;

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
    public void processGene(Gene gene) {
        try {
            for (Exon exon : gene.getExonList()) {
                HashMap<Integer, Integer> result = CoverageManager.getCoverage(exon);
                ss.accumulateCoverage(gene.getIndex(), result);
                outputCoverageDetailsByExon(result, gene, exon);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputCoverageDetailsByExon(HashMap<Integer, Integer> result,
            Gene gene, Exon e) throws IOException {
        Set<Integer> samples = result.keySet();
        for (Sample sample : SampleManager.getList()) {
            StringBuilder sb = new StringBuilder();
            sb.append(sample.getName()).append(",");
            sb.append(gene.getName()).append(",");
            sb.append(e.getChrStr()).append(",");
            sb.append(e.getIdStr()).append(",");
            sb.append(e.getStartPosition()).append(",");
            sb.append(e.getEndPosition()).append(",");
            sb.append(e.getLength()).append(",");

            int cov = 0;
            if (samples.contains(sample.getId())) {
                cov = result.get(sample.getId());

            }
            sb.append(cov).append(",");

            int pass;
            if (e.getLength() > 0) {
                double ratio = FormatManager.devide(cov, e.getLength());
                sb.append(FormatManager.getSixDegitDouble(ratio)).append(",");
                pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
            } else {
                sb.append("NA").append(",");
                pass = 0;
            }
            sb.append(pass);

            sb.append("\n");

            bwCoverageDetailsByExon.write(sb.toString());
        }
    }

    @Override
    public String toString() {
        return "It is running coverage summary function...";
    }
}
