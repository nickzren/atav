/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package function.coverage.base;

import function.coverage.base.Exon;
import function.coverage.base.Gene;
import function.genotype.base.SampleManager;
import utils.CommandValue;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author qwang
 */
public class ExonClean {

    private ArrayList<SortedExon> SortedExonList = new ArrayList<SortedExon>();
    private int TotalBases = 0;
    private int TotalCleanedBases = 0;
    private double CaseCoverage = 0;
    private double ControlCoverage = 0;

    public ExonClean(String inputfile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputfile));
            String str;
            int LineCount = 0;
            while ((str = br.readLine()) != null && str.length() > 0) {
                if (LineCount > 0) { //skip the headline
                    try {
                        String[] fields = str.split(",");
                        SortedExonList.add(new SortedExon(fields[0], Double.parseDouble(fields[2]),
                                Double.parseDouble(fields[3]), Double.parseDouble(fields[4]),
                                Integer.parseInt(fields[5])));
                        TotalBases += Integer.parseInt(fields[5]);
                    } catch (NumberFormatException e) {
                        LogManager.writeAndPrint("Invalid Exon Summary file format: " + str);
                    }
                }
                LineCount++;
            }
            br.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
        Collections.sort(SortedExonList);

    }

    public int GetTotalBases() {
        return TotalBases;
    }

    public int GetTotalCleanedBases() {
        return TotalCleanedBases;
    }

    public double GetCaseCoverage() {
        return CaseCoverage;
    }

    public double GetControlCoverage() {
        return ControlCoverage;
    }

    public double GetAllCoverage() {
        return (CaseCoverage * SampleManager.getCaseNum() + ControlCoverage * SampleManager.getCtrlNum()) / SampleManager.getListSize();
    }

    public int GetNumberOfExons() {
        return SortedExonList.size();
    }

    public double GetCutoff() {
        int i;
        double cutoff;
        double[] data = new double[SortedExonList.size()];
        double meandata = 0.0;
        //calculate mean data
        for (i = 0; i < data.length; i++) {
            data[i] = SortedExonList.get(i).Coverage_Difference;
            meandata += data[i];
        }
        meandata /= data.length;

        double total_variation = 0.0;
        for (i = 0; i < data.length; i++) {
            data[i] = (data[i] - meandata) * (data[i] - meandata);
            total_variation += data[i];
        }

        data[0] /= total_variation;
        for (i = 1; i < data.length; i++) {
            data[i] = data[i - 1] + data[i] / total_variation;
        }

        for (i = 0; i < data.length; i++) {
            data[i] = data[i] - (double) (i + 1) / (double) data.length;
        }

        int index = 0;
        double max_value = Double.MIN_VALUE;
        for (i = 0; i < data.length; i++) {
            if (data[i] > max_value) {
                max_value = data[i];
                index = i;
            }
        }
        cutoff = SortedExonList.get(index).Coverage_Difference;
        return cutoff;

    }

    public HashSet<String> GetExonCleanList(double cutoff) {
        TotalCleanedBases = 0;
        CaseCoverage = 0;
        ControlCoverage = 0;
        HashSet<String> result = new HashSet<String>();
        for (int i = 0; i < SortedExonList.size(); i++) {
            if (SortedExonList.get(i).Coverage_Difference < cutoff
                    && SortedExonList.get(i).Case_Average + SortedExonList.get(i).Control_Avarage > 0) {
                TotalCleanedBases += SortedExonList.get(i).Size;
                ControlCoverage += SortedExonList.get(i).Control_Avarage * SortedExonList.get(i).Size;
                CaseCoverage += SortedExonList.get(i).Case_Average * SortedExonList.get(i).Size;
                result.add(SortedExonList.get(i).Name);
            }
        }
        if (TotalBases > 0) {
            ControlCoverage /= TotalBases;
            CaseCoverage /= TotalBases;
        }
        return result;
    }

    public String GetCleanedGeneString(Gene gene, HashSet<String> cleanedExons) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        sb.append(gene.getName());
        sb.append(" ").append(gene.chr).append(" (");
        boolean isFirst = true;
        for (Iterator r = gene.getExonList().iterator(); r.hasNext();) {
            Exon exon = (Exon) r.next();
            String exonid = gene.getName() + "_" + exon.getStableId();
            if (cleanedExons.contains(exonid)) {
                size += exon.getCoveredRegion().getLength();
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(",");
                }
                sb.append(exon.getCoveredRegion().getStartPosition());
                sb.append("..").append(exon.getCoveredRegion().getEndPosition());
            }
        }
        sb.append(") ").append(size);
        if (size > 0 && !gene.chr.isEmpty()) {
            return sb.toString();
        } else {
            return "";
        }
    }

    public String GetCleanedGeneSummaryString(Gene gene, HashSet<String> cleanedExons) {
        HashMap<String, SortedExon> ExonMap = new HashMap<String, SortedExon>();
        for (int i = 0; i < SortedExonList.size(); i++) {
            ExonMap.put(SortedExonList.get(i).Name, SortedExonList.get(i));
        }
        int GeneSize = 0;
        double CaseAvg = 0;
        double CtrlAvg = 0;
        for (Iterator r = gene.getExonList().iterator(); r.hasNext();) {
            Exon exon = (Exon) r.next();
            String exonid = gene.getName() + "_" + exon.getStableId();
            if (cleanedExons.contains(exonid)) {
                SortedExon se = ExonMap.get(exonid);
                GeneSize += se.Size;
                CaseAvg += (double) se.Size * se.Case_Average;
                CtrlAvg += (double) se.Size * se.Control_Avarage;
            }
        }
        StringBuilder sb = new StringBuilder();

        if (GeneSize > 0) {
            CaseAvg /= (double) GeneSize;
            CtrlAvg /= (double) GeneSize;
        }
        NumberFormat pformat6 = new DecimalFormat("0.######");
        sb.append(gene.getName());
        sb.append(",").append(gene.chr);
        sb.append(",").append(gene.getLength());
        sb.append(",").append(pformat6.format(CaseAvg));
        sb.append(",").append(pformat6.format(CtrlAvg));
        double abs_diff = Math.abs(CaseAvg - CtrlAvg);
        sb.append(",").append(pformat6.format(abs_diff));
        sb.append(",").append(GeneSize);
        if (abs_diff > CommandValue.geneCleanCutoff) {
            if (CaseAvg < CtrlAvg) {
                sb.append(",").append("bias against discovery");
            } else {
                sb.append(",").append("bias for discovery");
            }

        } else {
            sb.append(",").append("none");
        }

        return sb.toString();
    }

    class SortedExon implements Comparable {

        String Name;
        public double Coverage_Difference;
        public double Case_Average;
        public double Control_Avarage;
        public int Size;

        public SortedExon(String name, double caseavg, double controlavg, double diff, int exonsize) {
            Name = name;
            Coverage_Difference = diff;
            Case_Average = caseavg;
            Control_Avarage = controlavg;
            Size = exonsize;
        }

        public int compareTo(Object other) {
            SortedExon that = (SortedExon) other;
            return Double.compare(that.Coverage_Difference, this.Coverage_Difference); // large -> small
        }
    }
}
