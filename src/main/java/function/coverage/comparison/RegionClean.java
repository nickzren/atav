package function.coverage.comparison;

import function.annotation.base.Gene;
import function.annotation.base.Exon;
import function.coverage.base.CoverageCommand;
import function.genotype.base.SampleManager;
import global.Data;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author qwang, nick
 */
public class RegionClean {

    private int totalBases = 0;
    private int totalCleanedBases = 0;
    private double caseCoverage = 0;
    private double ctrlCoverage = 0;
    private ArrayList<SortedRegion> sortedRegionList = new ArrayList<>();
    private HashMap<String, SortedRegion> sortedRegionMap = new HashMap<>();

    public RegionClean(String inputfile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputfile));
            String str;
            int LineCount = 0;
            while ((str = br.readLine()) != null && str.length() > 0) {
                if (LineCount > 0) { //skip the headline
                    try {
                        String[] fields = str.split(",");

                        sortedRegionList.add(
                                new SortedRegion(fields[0],
                                        Double.parseDouble(fields[2]),
                                        Double.parseDouble(fields[3]),
                                        Double.parseDouble(fields[4]),
                                        Integer.parseInt(fields[5])));

                        totalBases += Integer.parseInt(fields[5]);
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

    public RegionClean() {
    }

    public void addRegionToList(String name, int caseCoverage, int ctrlCoverage) {
        double caseAverage = MathManager.devide(caseCoverage, SampleManager.getCaseNum());
        double ctrlAverage = MathManager.devide(ctrlCoverage, SampleManager.getCtrlNum());
        double abs_diff = MathManager.abs(caseAverage, ctrlAverage);

        sortedRegionList.add(new SortedRegion(name, caseAverage, ctrlAverage, abs_diff, 1));
        totalBases += 1;
    }

    public int getTotalBases() {
        return totalBases;
    }

    public int getTotalCleanedBases() {
        return totalCleanedBases;
    }

    public double getCaseCoverage() {
        return caseCoverage;
    }

    public double getCtrlCoverage() {
        return ctrlCoverage;
    }

    public double getAllCoverage() {
        return (caseCoverage * SampleManager.getCaseNum() + ctrlCoverage * SampleManager.getCtrlNum()) / SampleManager.getListSize();
    }

    public int getSortedRegionListSite() {
        return sortedRegionList.size();
    }

    private double getCutoff() {
        //make sure the list has included all data and sortd.
        Collections.sort(sortedRegionList);

        int i;
        double cutoff;
        double[] data = new double[sortedRegionList.size()];
        double meandata = 0.0;
        //calculate mean data
        for (i = 0; i < data.length; i++) {
            data[i] = sortedRegionList.get(i).coverageDifference;
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

        cutoff = sortedRegionList.get(index).coverageDifference;

        LogManager.writeAndPrint("\nThe automated cutoff value for absolute mean coverage difference for sites is " + Double.toString(cutoff));

        if (CoverageCommand.siteCleanCutoff != Data.NO_FILTER) {
            cutoff = CoverageCommand.siteCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value " + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
        }

        return cutoff;

    }

    public void initSortedRegionMap() {
        double cutoff = getCutoff();

        for (SortedRegion sortedRegion : sortedRegionList) {
            if (sortedRegion.coverageDifference < cutoff
                    && sortedRegion.caseAverage + sortedRegion.controlAvarage > 0) {
                totalCleanedBases += sortedRegion.size;
                ctrlCoverage += sortedRegion.controlAvarage * sortedRegion.size;
                caseCoverage += sortedRegion.caseAverage * sortedRegion.size;

                sortedRegionMap.put(sortedRegion.name, sortedRegion);
            }
        }

        if (totalBases > 0) {
            ctrlCoverage /= totalBases;
            caseCoverage /= totalBases;
        }
    }

    public int getSortedRegionMapSize() {
        return sortedRegionMap.size();
    }

    public String getCleanedGeneStrBySite(Gene gene) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        sb.append(gene.getName());
        sb.append(" ").append(gene.getChr()).append(" (");
        boolean isFirst = true;
        for (Exon exon : gene.getExonList()) {
            int start = exon.getStartPosition();
            int end = exon.getEndPosition();

            int previouStartPosition = Data.NA;
            int previousEndPosition = Data.NA;
            for (int currentPosition = start; currentPosition <= end; currentPosition++) {
                String regionIdStr = gene.getName() + "_" + gene.getChr() + "_" + currentPosition;
                if (sortedRegionMap.containsKey(regionIdStr)) {
                    if (previouStartPosition == Data.NA) {
                        previouStartPosition = currentPosition;
                    }
                    size += 1;
                    previousEndPosition = currentPosition;
                } else { //there are gaps within the exon, so record the previos discovered region if any
                    //need to record the new discovered region
                    if (previouStartPosition != Data.NA) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            sb.append(",");
                        }
                        sb.append(previouStartPosition);
                        sb.append("..").append(previousEndPosition);
                        //reset to no region
                        previouStartPosition = Data.NA;
                        previousEndPosition = Data.NA;
                    }
                }
            }
            //repeat the recoring for last potential region
            //make sure this code is consistent with the code in previous section
            if (previouStartPosition != Data.NA) {
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

    public String getCleanedGeneStrByExon(Gene gene) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        sb.append(gene.getName());
        sb.append(" ").append(gene.getChr()).append(" (");
        boolean isFirst = true;
        for (Exon exon : gene.getExonList()) {
            String exonIdStr = gene.getName() + "_" + exon.getIdStr();
            if (sortedRegionMap.containsKey(exonIdStr)) {
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

    public String getCleanedGeneSummaryStrBySite(Gene gene) {
        int geneSize = 0;
        double caseAvg = 0;
        double ctrlAvg = 0;
        for (Exon exon : gene.getExonList()) {
            int start = exon.getStartPosition();
            int end = exon.getEndPosition();
            for (int currentPosition = start; currentPosition <= end; currentPosition++) {
                String regionIdStr = gene.getName() + "_" + gene.getChr() + "_" + currentPosition;
                SortedRegion sortedRegion = sortedRegionMap.get(regionIdStr);
                if (sortedRegion != null) {
                    geneSize++;
                    caseAvg += sortedRegion.caseAverage;
                    ctrlAvg += sortedRegion.controlAvarage;
                }
            }
        }

        return getGeneStr(gene, geneSize, caseAvg, ctrlAvg);
    }

    public String getCleanedGeneSummaryStrByExon(Gene gene) {
        int geneSize = 0;
        double caseAvg = 0;
        double ctrlAvg = 0;
        for (Exon exon : gene.getExonList()) {
            String regionIdStr = gene.getName() + "_" + exon.getIdStr();
            SortedRegion sortedRegion = sortedRegionMap.get(regionIdStr);
            if (sortedRegion != null) {
                geneSize += sortedRegion.size;
                caseAvg += (double) sortedRegion.size * sortedRegion.caseAverage;
                ctrlAvg += (double) sortedRegion.size * sortedRegion.controlAvarage;
            }
        }

        return getGeneStr(gene, geneSize, caseAvg, ctrlAvg);
    }

    private String getGeneStr(Gene gene, int geneSize, double caseAvg, double ctrlAvg) {
        StringBuilder sb = new StringBuilder();

        if (geneSize > 0) {
            caseAvg /= (double) geneSize;
            ctrlAvg /= (double) geneSize;
        }

        sb.append(gene.getName()).append(",");
        sb.append(gene.getChr()).append(",");
        sb.append(gene.getLength()).append(",");
        sb.append(FormatManager.getSixDegitDouble(caseAvg)).append(",");
        sb.append(FormatManager.getSixDegitDouble(ctrlAvg)).append(",");
        double abs_diff = MathManager.abs(caseAvg, ctrlAvg);
        sb.append(FormatManager.getSixDegitDouble(abs_diff)).append(",");
        sb.append(geneSize).append(",");
        if (abs_diff != Data.NA
                && abs_diff > CoverageCommand.geneCleanCutoff) {
            if (caseAvg < ctrlAvg) {
                sb.append("bias against discovery").append(",");
            } else {
                sb.append("bias for discovery").append(",");
            }

        } else {
            sb.append("none");
        }

        return sb.toString();
    }

    class SortedRegion implements Comparable {

        String name;
        public double coverageDifference;
        public double caseAverage;
        public double controlAvarage;
        public int size;

        public SortedRegion(String name, double caseavg, double controlavg, double diff, int regionsize) {
            this.name = name;
            coverageDifference = diff;
            caseAverage = caseavg;
            controlAvarage = controlavg;
            size = regionsize;
        }

        public int compareTo(Object other) {
            SortedRegion that = (SortedRegion) other;
            return Double.compare(that.coverageDifference, this.coverageDifference); // large -> small
        }
    }
}
