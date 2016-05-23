package function.coverage.base;

import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.annotation.base.GeneManager;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import java.io.BufferedWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * @author quanli
 */
public class SampleStatistics {

    int[] aSampleRegionCoverage;
    int[] aLength;
    double[][] aCoverage;

    NumberFormat pformat6 = new DecimalFormat("0.######");

    public SampleStatistics(int NumberOfRecord) {
        aLength = new int[NumberOfRecord];
        aSampleRegionCoverage = new int[SampleManager.getListSize()];
        aCoverage = new double[NumberOfRecord][SampleManager.getListSize()];
    }

    public void accumulateCoverage(int record, HashMap<Integer, Integer> result) {
        Set<Integer> samples = result.keySet();
        for (Iterator it = samples.iterator(); it.hasNext();) {
            int sampleid = (Integer) it.next();

            int column = SampleManager.getIndexById(sampleid);
            aCoverage[record][column] = aCoverage[record][column] + result.get(sampleid);
        }
    }

    public void print(HashMap<Integer, Integer> result, Gene gene, Exon e, BufferedWriter bw) throws Exception {
        if (CoverageCommand.isByExon) {
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
                    double ratio = (double) cov / (double) e.getLength();
                    sb.append(pformat6.format(ratio)).append(",");
                    pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
                } else {
                    sb.append("NA").append(",");
                    pass = 0;
                }
                sb.append(pass);

                sb.append("\n");
                bw.write(sb.toString());
            }
        }
    }

    public void setLength(int record, int length) {
        aLength[record] = length;
    }

    public void printMatrixHeader(BufferedWriter bw, boolean by_exon) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (by_exon) {
            sb.append(" ,Chr,Start_Position,Stop_Position,Size");
        } else {
            sb.append(" ,Chr");
        }

        for (Sample sample : SampleManager.getList()) {
            sb.append(",").append(sample.getName());
        }
        sb.append("\n");
        bw.write(sb.toString());
    }

    public void printMatrixRow(int record, Gene gene, BufferedWriter bw) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(gene.getName());
        sb.append(",").append(gene.getChr());
        for (Sample sample : SampleManager.getList()) {
            if (aLength[record] > 0) {
                //we only save scaled int in database, so need to do this to make output consistant
                int scaledCoverageRatio = (int) (aCoverage[record][sample.getIndex()] / aLength[record] * 10000);
                double ratio = (double) scaledCoverageRatio / 10000.0;
                sb.append(",").append(pformat6.format(ratio));
            } else {
                sb.append(",").append("NA");
            }
        }
        sb.append("\n");
        bw.write(sb.toString());
    }

    public void printMatrixRowbyExon(int record, HashMap<Integer, Integer> result, Gene gene, Exon e, BufferedWriter bw) throws Exception {
        if (CoverageCommand.isByExon) {
            Set<Integer> samples = result.keySet();
            StringBuilder sb = new StringBuilder();
            sb.append(gene.getName()).append("_").append(e.getIdStr());
            sb.append(",").append(gene.getChr());
            sb.append(",").append(e.getStartPosition());
            sb.append(",").append(e.getEndPosition());
            sb.append(",").append(e.getLength());
            for (Sample sample : SampleManager.getList()) {
                int cov = 0;
                if (samples.contains(sample.getId())) {
                    cov = result.get(sample.getId());
                }

                if (e.getLength() > 0) {
                    double ratio = (double) cov / (double) e.getLength();
                    sb.append(",").append(pformat6.format(ratio));
                } else {
                    sb.append(",").append("NA");
                }

            }
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    public void printGeneSummary(int record, Gene gene, BufferedWriter bw) throws Exception {
        if (SampleManager.getCaseNum() == 0 || SampleManager.getCtrlNum() == 0) {
            return;
        }
        if (aLength[record] == 0) {
            return;
        }

        double avgCase = 0;
        double avgCtrl = 0;
        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()) {
                avgCase = avgCase + aCoverage[record][sample.getIndex()];
            } else {
                avgCtrl = avgCtrl + aCoverage[record][sample.getIndex()];
            }
        }

        avgCase = avgCase / SampleManager.getCaseNum() / (double) aLength[record];
        avgCtrl = avgCtrl / SampleManager.getCtrlNum() / (double) aLength[record];
        StringBuilder sb = new StringBuilder();
        sb.append(gene.getName());
        sb.append(",").append(gene.getChr());
        sb.append(",").append(pformat6.format(avgCase));
        sb.append(",").append(pformat6.format(avgCtrl));
        double abs_diff = Math.abs(avgCase - avgCtrl);
        sb.append(",").append(pformat6.format(abs_diff));
        sb.append(",").append(aLength[record]);
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

    public void printGeneSummaryLinearTrait(int record, Gene gene, BufferedWriter bw) throws Exception {
        if (aLength[record] == 0) {
            return;
        }
        double avgAll = 0;
        for (Sample sample : SampleManager.getList()) {
            avgAll = avgAll + aCoverage[record][sample.getIndex()];
        }
        avgAll = avgAll / SampleManager.getListSize() / (double) aLength[record];
        StringBuilder sb = new StringBuilder();
        sb.append(gene.getName());
        sb.append(",").append(gene.getChr());
        sb.append(",").append(pformat6.format(avgAll));
        sb.append(",").append(aLength[record]);
        sb.append("\n");
        bw.write(sb.toString());
    }

    public void printExonSummaryLinearTrait(HashMap<Integer, Integer> result, Gene gene, Exon exon, BufferedWriter bw) throws Exception {
        if (CoverageCommand.isByExon) {
            Set<Integer> samples = result.keySet();
            double RegoinLength = exon.getLength();
            double avgAll = 0;
            SimpleRegression sr = new SimpleRegression(true);
            SummaryStatistics lss = new SummaryStatistics();
            for (Sample sample : SampleManager.getList()) {
                double cov = 0;
                if (samples.contains(sample.getId())) {
                    cov = result.get(sample.getId());
                }
                avgAll = avgAll + cov;
                double x = sample.getQuantitativeTrait();
                double y = cov / RegoinLength;
                sr.addData(x, y);
                lss.addValue(y);
            }
            avgAll = avgAll / SampleManager.getListSize() / RegoinLength;
            double R2 = sr.getRSquare();
            double pValue = sr.getSignificance();
            double Variance = lss.getVariance();

            StringBuilder sb = new StringBuilder();
            sb.append(gene.getName()).append("_").append(exon.getIdStr());
            sb.append(",").append(gene.getChr());
            sb.append(",").append(pformat6.format(avgAll));
            if (Double.isNaN(pValue)) { //happens if all coverages are the same
                sb.append(",").append(1);     //do not format here as we need to reuse it for precision
                sb.append(",").append(0);
            } else {
                sb.append(",").append(pValue); //do not format here as we need to reuse it for precision
                sb.append(",").append(R2 * 100);
            }
            sb.append(",").append(Variance);

            sb.append(",").append(exon.getLength());
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    public void printExonSummary(HashMap<Integer, Integer> result, Gene gene, Exon exon, BufferedWriter bw) throws Exception {
        if (SampleManager.getCaseNum() == 0 || SampleManager.getCtrlNum() == 0) {
            return;
        }
        if (CoverageCommand.isByExon) {
            Set<Integer> samples = result.keySet();

            double avgCase = 0;
            double avgCtrl = 0;
            for (Sample sample : SampleManager.getList()) {
                int cov = 0;
                if (samples.contains(sample.getId())) {
                    cov = result.get(sample.getId());

                }
                if (sample.isCase()) {
                    avgCase = avgCase + cov;
                } else {
                    avgCtrl = avgCtrl + cov;
                }
            }
            avgCase = avgCase / SampleManager.getCaseNum() / exon.getLength();
            avgCtrl = avgCtrl / SampleManager.getCtrlNum() / exon.getLength();

            StringBuilder sb = new StringBuilder();
            sb.append(gene.getName()).append("_").append(exon.getIdStr());
            sb.append(",").append(gene.getChr());
            sb.append(",").append(pformat6.format(avgCase));
            sb.append(",").append(pformat6.format(avgCtrl));
            sb.append(",").append(pformat6.format(Math.abs((avgCase - avgCtrl))));
            sb.append(",").append(exon.getLength());
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    //a hack here, Nick, please refactor to merge with one of the print function 
    //only used by siteCoverageComparison for some extra output
    public void updateSampleRegionCoverage(int record) {
        for (Sample sample : SampleManager.getList()) {
            int pass;
            if (aLength[record] > 0) {
                double ratio = aCoverage[record][sample.getIndex()] / aLength[record];
                pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
            } else {
                pass = 0;
            }
            //also accumalate region information here
            aSampleRegionCoverage[sample.getIndex()] = aSampleRegionCoverage[sample.getIndex()] + pass;
        }
    }

    public void print(int record, Gene gene, BufferedWriter bw) throws Exception {
        for (Sample sample : SampleManager.getList()) {
            StringBuilder sb = new StringBuilder();
            sb.append(sample.getName()).append(",");
            sb.append(gene.getName()).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(aLength[record]).append(",");
            sb.append((int) aCoverage[record][sample.getIndex()]).append(",");

            int pass;
            if (aLength[record] > 0) {
                double ratio = aCoverage[record][sample.getIndex()] / aLength[record];
                sb.append(pformat6.format(ratio)).append(",");
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
        for (int i = 0; i < GeneManager.getGeneBoundaryList().size(); i++) {
            TotalLength = TotalLength + aLength[i];
        }
        for (Sample sample : SampleManager.getList()) {
            int total_coverage = getSampleCoverageByIndex(sample.getIndex());
            double ratio = (double) total_coverage / (double) TotalLength;
            StringBuilder sb = new StringBuilder();
            sb.append(sample.getName()).append(",");
            sb.append(TotalLength).append(",");
            sb.append(total_coverage).append(",");
            sb.append(pformat6.format(ratio)).append(",");
            sb.append(GeneManager.getGeneBoundaryList().size()).append(",");
            sb.append(aSampleRegionCoverage[sample.getIndex()]).append(",");
            ratio = (double) aSampleRegionCoverage[sample.getIndex()] / (double) GeneManager.getGeneBoundaryList().size();
            sb.append(pformat6.format(ratio));
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    public int getSampleCoverageByIndex(int sampleIndex) {
        int CumResult = 0;
        for (int i = 0; i < aLength.length; i++) {
            CumResult = CumResult + (int) aCoverage[i][sampleIndex];
        }
        return CumResult;
    }
}
