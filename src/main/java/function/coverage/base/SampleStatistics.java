package function.coverage.base;

import function.annotation.base.Gene;
import function.annotation.base.GeneManager;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import utils.FormatManager;

/**
 *
 * @author quanli, nick
 */
public class SampleStatistics {

    int[] aSampleRegionCoverage;
    double[][] aCoverage;

    public SampleStatistics() {
        aSampleRegionCoverage = new int[SampleManager.getListSize()];
        aCoverage = new double[GeneManager.getGeneBoundaryList().size()][SampleManager.getListSize()];
    }

    public void accumulateCoverage(Gene gene, HashMap<Integer, Integer> result) {
        Set<Integer> samples = result.keySet();
        for (Iterator it = samples.iterator(); it.hasNext();) {
            int sampleid = (Integer) it.next();

            int column = SampleManager.getIndexById(sampleid);
            aCoverage[gene.getIndex()][column] = aCoverage[gene.getIndex()][column] + result.get(sampleid);
        }
    }

    public void printGeneSummary(Gene gene, BufferedWriter bw) throws Exception {
        if (SampleManager.getCaseNum() == 0 || SampleManager.getCtrlNum() == 0) {
            return;
        }
        if (gene.getLength() == 0) {
            return;
        }

        double avgCase = 0;
        double avgCtrl = 0;
        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()) {
                avgCase = avgCase + aCoverage[gene.getIndex()][sample.getIndex()];
            } else {
                avgCtrl = avgCtrl + aCoverage[gene.getIndex()][sample.getIndex()];
            }
        }

        avgCase = avgCase / SampleManager.getCaseNum() / (double) gene.getLength();
        avgCtrl = avgCtrl / SampleManager.getCtrlNum() / (double) gene.getLength();
        StringBuilder sb = new StringBuilder();
        sb.append(gene.getName());
        sb.append(",").append(gene.getChr());
        sb.append(",").append(FormatManager.getSixDegitDouble(avgCase));
        sb.append(",").append(FormatManager.getSixDegitDouble(avgCtrl));
        double abs_diff = Math.abs(avgCase - avgCtrl);
        sb.append(",").append(FormatManager.getSixDegitDouble(abs_diff));
        sb.append(",").append(gene.getLength());
        if (abs_diff > CoverageCommand.geneCleanCutoff) {
            if (avgCase < avgCtrl) {
                sb.append(",").append("bias against discovery");
            } else {
                sb.append(",").append("bias for discovery");
            }

        } else {
            sb.append(",").append("none");
        }
        sb.append("\n");
        bw.write(sb.toString());
    }

    public void printGeneSummaryLinearTrait(Gene gene, BufferedWriter bw) throws Exception {
        if (gene.getLength() == 0) {
            return;
        }
        double avgAll = 0;
        for (Sample sample : SampleManager.getList()) {
            avgAll = avgAll + aCoverage[gene.getIndex()][sample.getIndex()];
        }
        avgAll = avgAll / SampleManager.getListSize() / (double) gene.getLength();
        StringBuilder sb = new StringBuilder();
        sb.append(gene.getName());
        sb.append(",").append(gene.getChr());
        sb.append(",").append(FormatManager.getSixDegitDouble(avgAll));
        sb.append(",").append(gene.getLength());
        sb.append("\n");
        bw.write(sb.toString());
    }

    //a hack here, Nick, please refactor to merge with one of the print function 
    //only used by siteCoverageComparison for some extra output
    public void updateSampleRegionCoverage(Gene gene) {
        for (Sample sample : SampleManager.getList()) {
            int pass;
            if (gene.getLength() > 0) {
                double ratio = aCoverage[gene.getIndex()][sample.getIndex()] / gene.getLength();
                pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
            } else {
                pass = 0;
            }
            //also accumalate region information here
            aSampleRegionCoverage[sample.getIndex()] = aSampleRegionCoverage[sample.getIndex()] + pass;
        }
    }

    public void print(Gene gene, BufferedWriter bw) throws Exception {
        for (Sample sample : SampleManager.getList()) {
            StringBuilder sb = new StringBuilder();
            sb.append(sample.getName()).append(",");
            sb.append(gene.getName()).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(gene.getLength()).append(",");
            sb.append((int) aCoverage[gene.getIndex()][sample.getIndex()]).append(",");

            int pass;
            if (gene.getLength() > 0) {
                double ratio = aCoverage[gene.getIndex()][sample.getIndex()] / gene.getLength();
                sb.append(FormatManager.getSixDegitDouble(ratio)).append(",");
                pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
            } else {
                sb.append("NA").append(",");
                pass = 0;
            }
            //also accumalate region information here
            aSampleRegionCoverage[sample.getIndex()] = aSampleRegionCoverage[sample.getIndex()] + pass;
            sb.append(pass);
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    public void print(BufferedWriter bw) throws Exception {
        int TotalLength = 0;

        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            TotalLength = TotalLength + gene.getLength();
        }

        for (Sample sample : SampleManager.getList()) {
            int total_coverage = getSampleCoverageByIndex(sample.getIndex());
            double ratio = (double) total_coverage / (double) TotalLength;
            StringBuilder sb = new StringBuilder();
            sb.append(sample.getName()).append(",");
            sb.append(TotalLength).append(",");
            sb.append(total_coverage).append(",");
            sb.append(FormatManager.getSixDegitDouble(ratio)).append(",");
            sb.append(GeneManager.getGeneBoundaryList().size()).append(",");
            sb.append(aSampleRegionCoverage[sample.getIndex()]).append(",");
            ratio = (double) aSampleRegionCoverage[sample.getIndex()] / (double) GeneManager.getGeneBoundaryList().size();
            sb.append(FormatManager.getSixDegitDouble(ratio));
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    private int getSampleCoverageByIndex(int sampleIndex) {
        int CumResult = 0;
        for (int i = 0; i < GeneManager.getGeneBoundaryList().size(); i++) {
            CumResult = CumResult + (int) aCoverage[i][sampleIndex];
        }
        return CumResult;
    }
}
