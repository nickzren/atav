package function.coverage.comparison;

import function.annotation.base.Gene;
import function.annotation.base.Exon;
import function.coverage.base.CoverageCommand;
import global.Data;
import utils.LogManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import utils.FormatManager;

/**
 *
 * @author qwang, nick
 */
public class ExonCleanLinear {

    private int totalBases = 0;
    private int totalCleanedBases = 0;
    private double allCoverage = 0;

    private ArrayList<SortedExon> exonList = new ArrayList<>();
    private HashMap<String, SortedExon> cleanedExonMap = new HashMap<>();

    public void addExon(String name, double avgAll, double p, double r2, double variance, int regionSize) {
        exonList.add(new SortedExon(name, avgAll, p, r2, variance, regionSize));
        totalBases += regionSize;
    }
    
    public int getTotalBases() {
        return totalBases;
    }

    public int getTotalCleanedBases() {
        return totalCleanedBases;
    }

    public double getAllCoverage() {
        return allCoverage;
    }

    public int getExonListSize() {
        return exonList.size();
    }

    private double getCutoff() {
        Collections.sort(exonList);

        int i;
        double cutoff;
        double[] data = new double[exonList.size()];

        double total_variation = 0.0;
        for (i = 0; i < data.length; i++) {
            data[i] = exonList.get(i).Variance;
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

        cutoff = exonList.get(index).Variance;

        LogManager.writeAndPrint("\nThe automated cutoff value for variance for exons is " + Double.toString(cutoff));

        if (CoverageCommand.exonCleanCutoff != Data.NO_FILTER) {
            cutoff = CoverageCommand.exonCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value "
                    + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
        }

        return cutoff;
    }

    public void initCleanedExonMap() {
        double cutoff = getCutoff();

        totalCleanedBases = 0;
        allCoverage = 0;

        for (SortedExon sortedExon : exonList) {
            if (sortedExon.Variance < cutoff) {
                totalCleanedBases += sortedExon.Size;
                allCoverage += sortedExon.AvgAll * sortedExon.Size;
                cleanedExonMap.put(sortedExon.Name, sortedExon);
            }
        }

        if (totalBases > 0) {
            allCoverage /= totalBases;
        }
    }

    public int getCleanedExonMapSize() {
        return cleanedExonMap.size();
    }

    public String getCleanedGeneStrByExon(Gene gene) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        sb.append(gene.getName());
        sb.append(" ").append(gene.getChr()).append(" (");
        boolean isFirst = true;
        for (Exon exon : gene.getExonList()) {
            String regionIdStr = gene.getName() + "_" + exon.getIdStr();
            if (cleanedExonMap.containsKey(regionIdStr)) {
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

    public String getCleanedGeneSummaryStrByExon(Gene gene) {
        int geneSize = 0;
        double avgAll = 0;
        for (Exon exon : gene.getExonList()) {
            String regionId = gene.getName() + "_" + exon.getIdStr();

            SortedExon sortedExon = cleanedExonMap.get(regionId);
            if (sortedExon != null) {
                geneSize += sortedExon.Size;
                avgAll += (double) sortedExon.Size * sortedExon.AvgAll;
            }
        }

        return getGeneStr(gene, geneSize, avgAll);
    }

    private String getGeneStr(Gene gene, int geneSize, double avgAll) {
        StringBuilder sb = new StringBuilder();

        if (geneSize > 0) {
            avgAll /= (double) geneSize;
        }

        sb.append(gene.getName()).append(",");
        sb.append(gene.getChr()).append(",");
        sb.append(gene.getLength()).append(",");
        sb.append(FormatManager.getSixDegitDouble(avgAll)).append(",");
        sb.append(geneSize);

        return sb.toString();
    }

    class SortedExon implements Comparable {

        String Name;
        public double AvgAll;
        public double pValue;
        public double R2;
        public double Variance;
        public int Size;

        public SortedExon(String name, double avgAll, double p, double r2, double variance, int regionSize) {
            Name = name;
            AvgAll = avgAll;
            pValue = p;
            R2 = r2;
            Variance = variance;
            Size = regionSize;
        }

        @Override
        public int compareTo(Object other) {
            SortedExon that = (SortedExon) other;
            return Double.compare(that.Variance, this.Variance); // large -> small
        }
    }
}
