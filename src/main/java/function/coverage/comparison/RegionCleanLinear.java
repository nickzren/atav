package function.coverage.comparison;

import function.annotation.base.Gene;
import function.annotation.base.Exon;
import function.coverage.base.CoverageCommand;
import global.Data;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import utils.FormatManager;

/**
 *
 * @author qwang, nick
 */
public class RegionCleanLinear {

    private int totalBases = 0;
    private int totalCleanedBases = 0;
    private double allCoverage = 0;

    private ArrayList<SortedExon> sortedExonList = new ArrayList<>();
    private HashMap<String, SortedExon> sortedExonMap = new HashMap<>();

    public RegionCleanLinear(String inputfile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputfile));
            String str;
            int LineCount = 0;
            while ((str = br.readLine()) != null && str.length() > 0) {
                if (LineCount > 0) { //skip the headline
                    try {
                        String[] fields = str.split(",");

                        sortedExonList.add(
                                new SortedExon(fields[0],
                                        Double.parseDouble(fields[2]),
                                        Double.parseDouble(fields[3]),
                                        Double.parseDouble(fields[4]),
                                        Double.parseDouble(fields[5]),
                                        Integer.parseInt(fields[6])));

                        totalBases += Integer.parseInt(fields[6]);
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

    public int getSortedExonListSize() {
        return sortedExonList.size();
    }

    private double getCutoff() {
        Collections.sort(sortedExonList);

        int i;
        double cutoff;
        double[] data = new double[sortedExonList.size()];

        double total_variation = 0.0;
        for (i = 0; i < data.length; i++) {
            data[i] = sortedExonList.get(i).Variance;
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

        cutoff = sortedExonList.get(index).Variance;

        LogManager.writeAndPrint("\nThe automated cutoff value for variance for exons is " + Double.toString(cutoff));

        if (CoverageCommand.exonCleanCutoff != Data.NO_FILTER) {
            cutoff = CoverageCommand.exonCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value "
                    + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
        }

        return cutoff;
    }

    public void initSortedExonSet() {
        double cutoff = getCutoff();

        totalCleanedBases = 0;
        allCoverage = 0;

        for (SortedExon sortedExon : sortedExonList) {
            if (sortedExon.Variance < cutoff) {
                totalCleanedBases += sortedExon.Size;
                allCoverage += sortedExon.AvgAll * sortedExon.Size;
                sortedExonMap.put(sortedExon.Name, sortedExon);
            }
        }

        if (totalBases > 0) {
            allCoverage /= totalBases;
        }
    }

    public int getSortedExonMapSize() {
        return sortedExonMap.size();
    }

    public String getCleanedGeneString(Gene gene) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        sb.append(gene.getName());
        sb.append(" ").append(gene.getChr()).append(" (");
        boolean isFirst = true;
        for (Exon exon : gene.getExonList()) {
            String regionIdStr = gene.getName() + "_" + exon.getIdStr();
            if (sortedExonMap.containsKey(regionIdStr)) {
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

            SortedExon sortedExon = sortedExonMap.get(regionId);
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

        public SortedExon(String name, double avgall, double p, double r2, double variance, int exonsize) {
            Name = name;
            AvgAll = avgall;
            pValue = p;
            R2 = r2;
            Variance = variance;
            Size = exonsize;
        }

        public int compareTo(Object other) {
            SortedExon that = (SortedExon) other;
            return Double.compare(that.Variance, this.Variance); // large -> small
        }
    }
}
