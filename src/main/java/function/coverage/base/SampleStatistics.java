package function.coverage.base;

import function.annotation.base.Exon;
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

    HashMap<Integer, Integer> Id2Index = new HashMap<Integer, Integer>();
    HashMap<Integer, Integer> Index2Id = new HashMap<Integer, Integer>();
    int[] aLength;
    int[] aSampleRegionCoverage;
    double[][] aCoverage;
    String[] SampleNames;
    String[] RecordNames;
    String[] RecordChrs;
    
    NumberFormat pformat6 = new DecimalFormat("0.######");

    public SampleStatistics(int NumberOfRecord) {
        aLength = new int[NumberOfRecord];
        RecordNames = new String[NumberOfRecord];
        RecordChrs = new String[NumberOfRecord];
        aSampleRegionCoverage = new int[SampleManager.getListSize()];
        aCoverage = new double[NumberOfRecord][SampleManager.getListSize()];
        SampleNames = new String[SampleManager.getListSize()];

        for (int i = 0; i < SampleManager.getListSize(); i++) {
            Sample sample = SampleManager.getList().get(i);
            SampleNames[i] = sample.getName();
            Id2Index.put(sample.getId(), i);
            Index2Id.put(i, sample.getId());
        }
    }

    public void setRecordName(int record, String name, String chr) {
        RecordNames[record] = name;
        RecordChrs[record] = chr;
    }

    public void accumulateCoverage(int record, HashMap<Integer, Integer> result) {
        Set<Integer> samples = result.keySet();
        for (Iterator it = samples.iterator(); it.hasNext();) {
            int sampleid = (Integer) it.next();
            int column = Id2Index.get(sampleid);
            aCoverage[record][column] = aCoverage[record][column] + result.get(sampleid);
        }
    }

    public void setGeneCoverage(int record, HashMap<Integer, Double> result) {
        Set<Integer> samples = result.keySet();
        for (Iterator it = samples.iterator(); it.hasNext();) {
            int sampleid = (Integer) it.next();
            int column = Id2Index.get(sampleid);
            aCoverage[record][column] = result.get(sampleid) * aLength[record];
        }
    }

    public void print(int record, HashMap<Integer, Integer> result, Exon e, BufferedWriter bw) throws Exception {
        if (CoverageCommand.isByExon) {
            Set<Integer> samples = result.keySet();
            for (int i = 0; i < SampleNames.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(SampleNames[i]).append(",");
                sb.append(RecordNames[record]).append(",");
                sb.append(RecordChrs[record]).append(",");
                sb.append(e.getIdStr()).append(",");
                sb.append(e.getStartPosition()).append(",");
                sb.append(e.getEndPosition()).append(",");
                sb.append(e.getLength()).append(",");
                int sampleid = Index2Id.get(i);
                int cov = 0;
                if (samples.contains(sampleid)) {
                    cov = result.get(sampleid);

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

        for (int i = 0; i < SampleNames.length; i++) {
            sb.append(",").append(SampleNames[i]);
        }
        sb.append("\n");
        bw.write(sb.toString());
    }

    public void printMatrixRow(int record, BufferedWriter bw) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(RecordNames[record]);
        sb.append(",").append(RecordChrs[record]);
        for (int i = 0; i < SampleNames.length; i++) {
            if (aLength[record] > 0) {
                //we only save scaled int in database, so need to do this to make output consistant
                int scaledCoverageRatio = (int) (aCoverage[record][i] / aLength[record] * 10000);
                double ratio = (double) scaledCoverageRatio / 10000.0;
                sb.append(",").append(pformat6.format(ratio));
            } else {
                sb.append(",").append("NA");
            }
        }
        sb.append("\n");
        bw.write(sb.toString());
    }

    public void printMatrixRowbyExon(int record, HashMap<Integer, Integer> result, Exon e, BufferedWriter bw) throws Exception {
        if (CoverageCommand.isByExon) {
            Set<Integer> samples = result.keySet();
            StringBuilder sb = new StringBuilder();
            sb.append(RecordNames[record]).append("_").append(e.getIdStr());
            sb.append(",").append(RecordChrs[record]);
            sb.append(",").append(e.getStartPosition());
            sb.append(",").append(e.getEndPosition());
            sb.append(",").append(e.getLength());
            for (int i = 0; i < SampleNames.length; i++) {
                int sampleid = Index2Id.get(i);
                int cov = 0;
                if (samples.contains(sampleid)) {
                    cov = result.get(sampleid);

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

    public void printGeneSummary(int record, BufferedWriter bw) throws Exception {
        if (SampleManager.getCaseNum() == 0 || SampleManager.getCtrlNum() == 0) {
            return;
        }
        if (aLength[record] == 0) {
            return;
        }

        double avgCase = 0;
        double avgCtrl = 0;
        for (int i = 0; i < SampleNames.length; i++) {
            int sampleid = Index2Id.get(i);
            if (SampleManager.getMap().get(sampleid).isCase()) {
                avgCase = avgCase + aCoverage[record][i];
            } else {
                avgCtrl = avgCtrl + aCoverage[record][i];
            }
        }
        avgCase = avgCase / SampleManager.getCaseNum() / (double) aLength[record];
        avgCtrl = avgCtrl / SampleManager.getCtrlNum() / (double) aLength[record];
        StringBuilder sb = new StringBuilder();
        sb.append(RecordNames[record]);
        sb.append(",").append(RecordChrs[record]);
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
    public void printGeneSummaryLinearTrait(int record, BufferedWriter bw) throws Exception {
        if (aLength[record] == 0) {
            return;
        }
        double avgAll = 0;
        for (int i = 0; i < SampleNames.length; i++) {
            avgAll = avgAll + aCoverage[record][i];
        }
        avgAll = avgAll / SampleNames.length / (double) aLength[record];
        StringBuilder sb = new StringBuilder();
        sb.append(RecordNames[record]);
        sb.append(",").append(RecordChrs[record]);
        sb.append(",").append(pformat6.format(avgAll));
        sb.append(",").append(aLength[record]);
        sb.append("\n");
        bw.write(sb.toString());
    }
    public void printExonSummaryLinearTrait(int record, HashMap<Integer, Integer> result, Exon e, BufferedWriter bw) throws Exception {
        if (CoverageCommand.isByExon) {
            Set<Integer> samples = result.keySet();
            double RegoinLength = e.getLength();
            double avgAll = 0;
            SimpleRegression sr = new SimpleRegression(true);
            SummaryStatistics lss = new SummaryStatistics();
            for (int i = 0; i < SampleNames.length; i++) {
                int sampleid = Index2Id.get(i);
                double cov = 0;
                if (samples.contains(sampleid)) {
                    cov = result.get(sampleid);
                }
                avgAll = avgAll + cov;
                double x = SampleManager.getMap().get(sampleid).getQuantitativeTrait();
                double y = cov / RegoinLength;
                sr.addData(x, y);
                lss.addValue(y);
            }
            avgAll = avgAll / SampleNames.length / RegoinLength;
            double R2 = sr.getRSquare();
            double pValue = sr.getSignificance();
            double Variance = lss.getVariance();
            
            StringBuilder sb = new StringBuilder();
            sb.append(RecordNames[record]).append("_").append(e.getIdStr());
            sb.append(",").append(RecordChrs[record]);
            sb.append(",").append(pformat6.format(avgAll));
            if (Double.isNaN(pValue)) { //happens if all coverages are the same
                sb.append(",").append(1);     //do not format here as we need to reuse it for precision
                sb.append(",").append(0);
            } else {
                sb.append(",").append(pValue); //do not format here as we need to reuse it for precision
                sb.append(",").append(R2*100);
            }
            sb.append(",").append(Variance);
            
            sb.append(",").append(e.getLength());
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    public void printExonSummary(int record, HashMap<Integer, Integer> result, Exon e, BufferedWriter bw) throws Exception {
        if (SampleManager.getCaseNum() == 0 || SampleManager.getCtrlNum() == 0) {
            return;
        }
        if (CoverageCommand.isByExon) {
            Set<Integer> samples = result.keySet();

            double avgCase = 0;
            double avgCtrl = 0;
            for (int i = 0; i < SampleNames.length; i++) {
                int sampleid = Index2Id.get(i);
                int cov = 0;
                if (samples.contains(sampleid)) {
                    cov = result.get(sampleid);

                }
                if (SampleManager.getMap().get(sampleid).isCase()) {
                    avgCase = avgCase + cov;
                } else {
                    avgCtrl = avgCtrl + cov;
                }
            }
            avgCase = avgCase / SampleManager.getCaseNum() / e.getLength();
            avgCtrl = avgCtrl / SampleManager.getCtrlNum() / e.getLength();

            //if (avgCase + avgCtrl > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(RecordNames[record]).append("_").append(e.getIdStr());
            sb.append(",").append(RecordChrs[record]);
            sb.append(",").append(pformat6.format(avgCase));
            sb.append(",").append(pformat6.format(avgCtrl));
            sb.append(",").append(pformat6.format(Math.abs((avgCase - avgCtrl))));
            sb.append(",").append(e.getLength());
            sb.append("\n");
            bw.write(sb.toString());
            //}
        }
    }

    //a hack here, Nick, please refactor to merge with one of the print function 
    //only used by siteCoverageComparison for some extra output
    public void updateSampleRegionCoverage(int record) {
        for (int i = 0; i < SampleNames.length; i++) {
            int pass;
            if (aLength[record] > 0) {
                double ratio = aCoverage[record][i] / aLength[record];
                pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
            } else {
                pass = 0;
            }
            //also accumalate region information here
            aSampleRegionCoverage[i] = aSampleRegionCoverage[i] + pass;
        }
    }
    
    public void print(int record, BufferedWriter bw) throws Exception {
        for (int i = 0; i < SampleNames.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(SampleNames[i]).append(",");
            sb.append(RecordNames[record]).append(",");
            sb.append(RecordChrs[record]).append(",");
            sb.append(aLength[record]).append(",");
            sb.append((int) aCoverage[record][i]).append(",");

            int pass;
            if (aLength[record] > 0) {
                double ratio = aCoverage[record][i] / aLength[record];
                sb.append(pformat6.format(ratio)).append(",");
                pass = ratio >= CoverageCommand.minPercentRegionCovered ? 1 : 0;
            } else {
                sb.append("NA").append(",");
                pass = 0;
            }
            //also accumalate region information here
            aSampleRegionCoverage[i] = aSampleRegionCoverage[i] + pass;
            sb.append(pass);
            sb.append("\n");
            bw.write(sb.toString());
        }
    }

    public void print(BufferedWriter bw) throws Exception {
        int TotalLength = 0;
        for (int i = 0; i < RecordNames.length; i++) {
            TotalLength = TotalLength + aLength[i];
        }
        for (int i = 0; i < SampleNames.length; i++) {
            int total_coverage = getSampleCoverageByIndex(i);
            double ratio = (double) total_coverage / (double) TotalLength;
            StringBuilder sb = new StringBuilder();
            sb.append(SampleNames[i]).append(",");
            sb.append(TotalLength).append(",");
            sb.append(total_coverage).append(",");
            sb.append(pformat6.format(ratio)).append(",");
            sb.append(RecordNames.length).append(",");
            sb.append(aSampleRegionCoverage[i]).append(",");
            ratio = (double) aSampleRegionCoverage[i] / (double) RecordNames.length;
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
