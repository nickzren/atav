package function.coverage.comparison;

import function.annotation.base.Gene;
import function.annotation.base.Exon;
import function.coverage.base.CoverageCommand;
import function.genotype.base.SampleManager;
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
import utils.FormatManager;

/**
 *
 * @author qwang
 */
public class RegionClean {

    private ArrayList<SortedRegion> SortedRegionList = new ArrayList<SortedRegion>();
    private int TotalBases = 0;
    private int TotalCleanedBases = 0;
    private double CaseCoverage = 0;
    private double ControlCoverage = 0;
    private HashMap<String, SortedRegion> RegionMap = new HashMap<String, SortedRegion>();
    
    public RegionClean(String inputfile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputfile));
            String str;
            int LineCount = 0;
            while ((str = br.readLine()) != null && str.length() > 0) {
                if (LineCount > 0) { //skip the headline
                    try {
                        String[] fields = str.split(",");
                        SortedRegionList.add(new SortedRegion(fields[0], Double.parseDouble(fields[2]),
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
        Collections.sort(SortedRegionList);
    }

    public RegionClean() {
        // for site coverage comparison, instead of reading from output file
        // we allow the SortedRegionList to be accumalted when they are generated 
        SortedRegionList.clear(); 
    }
    
    public void AddRegionToList(String name, double caseavg, double controlavg, double diff) {
        //site has a fixed length of 1
        SortedRegionList.add(new SortedRegion(name, caseavg,controlavg,diff, 1));              
        TotalBases += 1;
    }
    
    public void FinalizeRegionList() {
        Collections.sort(SortedRegionList);
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

    public int GetNumberOfRegions() {
        return SortedRegionList.size();
    }

    public double GetCutoff() {
        int i;
        double cutoff;
        double[] data = new double[SortedRegionList.size()];
        double meandata = 0.0;
        //calculate mean data
        for (i = 0; i < data.length; i++) {
            data[i] = SortedRegionList.get(i).Coverage_Difference;
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
        cutoff = SortedRegionList.get(index).Coverage_Difference;
        return cutoff;

    }

    public HashSet<String> GetRegionCleanList(double cutoff) {
        TotalCleanedBases = 0;
        CaseCoverage = 0;
        ControlCoverage = 0;
        HashSet<String> result = new HashSet<String>();
        for (int i = 0; i < SortedRegionList.size(); i++) {
            if (SortedRegionList.get(i).Coverage_Difference < cutoff
                    && SortedRegionList.get(i).Case_Average + SortedRegionList.get(i).Control_Avarage > 0) {
                TotalCleanedBases += SortedRegionList.get(i).Size;
                ControlCoverage += SortedRegionList.get(i).Control_Avarage * SortedRegionList.get(i).Size;
                CaseCoverage += SortedRegionList.get(i).Case_Average * SortedRegionList.get(i).Size;
                result.add(SortedRegionList.get(i).Name);
            }
        }
        if (TotalBases > 0) {
            ControlCoverage /= TotalBases;
            CaseCoverage /= TotalBases;
        }
        return result;
    }

    public String GetCleanedGeneStringSite(Gene gene, HashSet<String> cleanedRegions) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        sb.append(gene.getName());
        sb.append(" ").append(gene.getChr()).append(" (");
        boolean isFirst = true;
        for (Iterator r = gene.getExonList().iterator(); r.hasNext();) { 
            Exon exon = (Exon) r.next();
            int startPosition = exon.getStartPosition();
            int endPosition = exon.getEndPosition();

            int previouStartPosition = -1;
            int previousEndPosition = -1;
            for (int currentPosition = startPosition; currentPosition <= endPosition; currentPosition++) {
                StringBuilder regionid = new StringBuilder();
                regionid.append(gene).append("_").append(gene.getChr()).append("_").append(currentPosition);
                if (cleanedRegions.contains(regionid.toString())) {
                    if (previouStartPosition < 0) {
                        previouStartPosition = currentPosition;
                    }
                    size += 1;
                    previousEndPosition = currentPosition;
                } else { //there are gaps within the exon, so record the previos discovered region if any
                    //need to record the new discovered region
                    if (previouStartPosition > 0) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            sb.append(",");
                        }
                        sb.append(previouStartPosition);
                        sb.append("..").append(previousEndPosition);
                        //reset to no region
                        previouStartPosition = -1;
                        previousEndPosition = -1;
                    }
                }
            }
            //repeat the recoring for last potential region
            //make sure this code is consistent with the code in previous section
            if (previouStartPosition > 0) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(",");
                }
                sb.append(previouStartPosition);
                sb.append("..").append(previousEndPosition);
            }
        }
        sb.append(") ").append(size);
        if (size > 0 && !gene.getChr().isEmpty()) {
            return sb.toString();
        } else {
            return "";
        }
    }
    public String GetCleanedGeneString(Gene gene, HashSet<String> cleanedRegions) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        sb.append(gene.getName());
        sb.append(" ").append(gene.getChr()).append(" (");
        boolean isFirst = true;
        for (Iterator r = gene.getExonList().iterator(); r.hasNext();) { 
        //reuse exon here, might need to change to a more approriate name later
        // Should be region in general
            Exon exon = (Exon) r.next();
            String exonid = gene.getName() + "_" + exon.getIdStr();
            if (cleanedRegions.contains(exonid)) {
                size += exon.getLength();
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(",");
                }
                sb.append(exon.getStartPosition());
                sb.append("..").append(exon.getEndPosition());
            }
        }
        sb.append(") ").append(size);
        if (size > 0 && !gene.getChr().isEmpty()) {
            return sb.toString();
        } else {
            return "";
        }
    }

    public String GetCleanedGeneSummaryString(Gene gene, HashSet<String> cleanedRegions, boolean isSite) {
        if (RegionMap.isEmpty()) {
            for (int i = 0; i < SortedRegionList.size(); i++) {
                RegionMap.put(SortedRegionList.get(i).Name, SortedRegionList.get(i));
            }
        }
        int GeneSize = 0;
        double CaseAvg = 0;
        double CtrlAvg = 0;
        for (Iterator r = gene.getExonList().iterator(); r.hasNext();) {
            Exon exon = (Exon) r.next();
            if (isSite) {
                int startPosition = exon.getStartPosition();
                int endPosition = exon.getEndPosition();
                for (int currentPosition = startPosition; currentPosition <= endPosition; currentPosition++) {
                    StringBuilder regionid = new StringBuilder();
                    regionid.append(gene).append("_").append(gene.getChr()).append("_").append(currentPosition);
                    if (cleanedRegions.contains(regionid.toString())) {
                        GeneSize++;
                        SortedRegion se = RegionMap.get(regionid.toString());
                        CaseAvg += se.Case_Average;
                        CtrlAvg += se.Control_Avarage;
                    }
                }
            } else {
                String exonid = gene.getName() + "_" + exon.getIdStr();
                if (cleanedRegions.contains(exonid)) {
                    SortedRegion se = RegionMap.get(exonid);
                    GeneSize += se.Size;
                    CaseAvg += (double) se.Size * se.Case_Average;
                    CtrlAvg += (double) se.Size * se.Control_Avarage;
                }
            }
        }
        StringBuilder sb = new StringBuilder();

        if (GeneSize > 0) {
            CaseAvg /= (double) GeneSize;
            CtrlAvg /= (double) GeneSize;
        }
        
        sb.append(gene.getName());
        sb.append(",").append(gene.getChr());
        sb.append(",").append(gene.getLength());
        sb.append(",").append(FormatManager.getSixDegitDouble(CaseAvg));
        sb.append(",").append(FormatManager.getSixDegitDouble(CtrlAvg));
        double abs_diff = Math.abs(CaseAvg - CtrlAvg);
        sb.append(",").append(FormatManager.getSixDegitDouble(abs_diff));
        sb.append(",").append(GeneSize);
        if (abs_diff > CoverageCommand.geneCleanCutoff) {
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

    class SortedRegion implements Comparable {

        String Name;
        public double Coverage_Difference;
        public double Case_Average;
        public double Control_Avarage;
        public int Size;

        public SortedRegion(String name, double caseavg, double controlavg, double diff, int regionsize) {
            Name = name;
            Coverage_Difference = diff;
            Case_Average = caseavg;
            Control_Avarage = controlavg;
            Size = regionsize;
        }

        public int compareTo(Object other) {
            SortedRegion that = (SortedRegion) other;
            return Double.compare(that.Coverage_Difference, this.Coverage_Difference); // large -> small
        }
    }
}
