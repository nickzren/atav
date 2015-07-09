package function.coverage.base;

import function.genotype.base.Sample;
import global.Data;
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
    int NextRecord;
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
        NextRecord = 0;
    }

    public int getNextRecord() {
        NextRecord++;
        return NextRecord - 1;
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
                sb.append(e.getStableId()).append(",");
                sb.append(e.covRegion.getStartPosition()).append(",");
                sb.append(e.covRegion.getEndPosition()).append(",");
                sb.append(e.getCoveredRegion().getLength()).append(",");
                int sampleid = Index2Id.get(i);
                int cov = 0;
                if (samples.contains(sampleid)) {
                    cov = result.get(sampleid);

                }
                sb.append(cov).append(",");

                int pass;
                if (e.getCoveredRegion().getLength() > 0) {
                    double ratio = (double) cov / (double) e.getCoveredRegion().getLength();
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
            sb.append(RecordNames[record]).append("_").append(e.getStableId());
            sb.append(",").append(RecordChrs[record]);
            sb.append(",").append(e.covRegion.getStartPosition());
            sb.append(",").append(e.covRegion.getEndPosition());
            sb.append(",").append(e.getCoveredRegion().getLength());
            for (int i = 0; i < SampleNames.length; i++) {
                int sampleid = Index2Id.get(i);
                int cov = 0;
                if (samples.contains(sampleid)) {
                    cov = result.get(sampleid);

                }

                if (e.getCoveredRegion().getLength() > 0) {
                    double ratio = (double) cov / (double) e.getCoveredRegion().getLength();
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
            if (SampleManager.getTable().get(sampleid).isCase()) {
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
            double RegoinLength = e.getCoveredRegion().getLength();
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
                double x = SampleManager.getTable().get(sampleid).getQuantitativeTrait();
                double y = cov / RegoinLength;
                sr.addData(x, y);
                lss.addValue(y);
            }
            avgAll = avgAll / SampleNames.length / RegoinLength;
            double R2 = sr.getRSquare();
            double pValue = sr.getSignificance();
            double Variance = lss.getVariance();
            
            StringBuilder sb = new StringBuilder();
            sb.append(RecordNames[record]).append("_").append(e.getStableId());
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
            
            sb.append(",").append(e.getCoveredRegion().getLength());
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
                if (SampleManager.getTable().get(sampleid).isCase()) {
                    avgCase = avgCase + cov;
                } else {
                    avgCtrl = avgCtrl + cov;
                }
            }
            avgCase = avgCase / SampleManager.getCaseNum() / e.getCoveredRegion().getLength();
            avgCtrl = avgCtrl / SampleManager.getCtrlNum() / e.getCoveredRegion().getLength();

            //if (avgCase + avgCtrl > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(RecordNames[record]).append("_").append(e.getStableId());
            sb.append(",").append(RecordChrs[record]);
            sb.append(",").append(pformat6.format(avgCase));
            sb.append(",").append(pformat6.format(avgCtrl));
            sb.append(",").append(pformat6.format(Math.abs((avgCase - avgCtrl))));
            sb.append(",").append(e.getCoveredRegion().getLength());
            sb.append("\n");
            bw.write(sb.toString());
            //}
        }
    }

    public void updateMatrixRowInDataBase(int record) throws Exception {
        int scaledCoverageRatio[] = new int[SampleNames.length];
        for (int i = 0; i < SampleNames.length; i++) {
            if (aLength[record] > 0) {
                scaledCoverageRatio[i] = (int) (aCoverage[record][i] / aLength[record] * 10000);
            } else {
                scaledCoverageRatio[i] = Data.NA; //should not happen in Slave's case
            }
        }

        DBUtils.updateGeneCoverageSummary(RecordNames[record], Index2Id, scaledCoverageRatio);
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

    public int getSampleCoverageById(int sampleid) {
        int column = Id2Index.get(sampleid);
        return getSampleCoverageByIndex(column);
    }

    public int getSampleCoverageByIndex(int sampleIndex) {
        int CumResult = 0;
        for (int i = 0; i < aLength.length; i++) {
            CumResult = CumResult + (int) aCoverage[i][sampleIndex];
        }
        return CumResult;
    }
}
