package function.coverage.comparison;

import function.annotation.base.Gene;
import function.annotation.base.Exon;
import function.coverage.base.CoverageCommand;
import function.genotype.base.SampleManager;
import global.Data;
import utils.LogManager;
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

    int totalBases = 0;
    int totalCleanedBases = 0;
    float caseCoverage = 0;
    float ctrlCoverage = 0;
    ArrayList<SortedRegion> regionList = new ArrayList<>();
    HashMap<String, SortedRegion> cleanedRegionMap = new HashMap<>();

    public void addExon(String name, float caseAvg, float ctrlAvg, float absDiff, int regionSize) {
        regionList.add(new SortedRegion(name, caseAvg, ctrlAvg, absDiff, regionSize));
        totalBases += regionSize;
    }

    private double getAllCoverage() {
        return (caseCoverage * SampleManager.getCaseNum() + ctrlCoverage * SampleManager.getCtrlNum()) / SampleManager.getListSize();
    }

    protected double getCutoff() {
        //make sure the list has included all data and sortd.
        Collections.sort(regionList);

        int i;
        float cutoff;
        float[] data = new float[regionList.size()];
        float meandata = 0;
        //calculate mean data
        for (i = 0; i < data.length; i++) {
            data[i] = regionList.get(i).getCovDiff();
            meandata += data[i];
        }
        meandata /= data.length;

        float total_variation = 0;
        for (i = 0; i < data.length; i++) {
            data[i] = (data[i] - meandata) * (data[i] - meandata);
            total_variation += data[i];
        }

        data[0] /= total_variation;
        for (i = 1; i < data.length; i++) {
            data[i] = data[i - 1] + data[i] / total_variation;
        }

        for (i = 0; i < data.length; i++) {
            data[i] = data[i] - (float) (i + 1) / (float) data.length;
        }

        int index = 0;
        double max_value = Double.MIN_VALUE;
        for (i = 0; i < data.length; i++) {
            if (data[i] > max_value) {
                max_value = data[i];
                index = i;
            }
        }

        cutoff = regionList.get(index).getCovDiff();

        LogManager.writeAndPrint("\nThe automated cutoff value for absolute mean coverage difference for sites is " + Float.toString(cutoff));

        if (CoverageCommand.siteCleanCutoff != Data.NO_FILTER) {
            cutoff = CoverageCommand.siteCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value " + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
        }

        return cutoff;

    }

    public void initCleanedRegionMap() {
        double cutoff = getCutoff();

        for (SortedRegion sortedRegion : regionList) {
            if (sortedRegion.getCutoff() < cutoff
                    && sortedRegion.getCaseAvg() + sortedRegion.getCtrlAvg() > 0) {
                totalCleanedBases += sortedRegion.getLength();
                ctrlCoverage += sortedRegion.getCtrlAvg() * sortedRegion.getLength();
                caseCoverage += sortedRegion.getCaseAvg() * sortedRegion.getLength();

                cleanedRegionMap.put(sortedRegion.getName(), sortedRegion);
            }
        }

        caseCoverage = MathManager.devide(caseCoverage, totalBases);
        ctrlCoverage = MathManager.devide(ctrlCoverage, totalBases);
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
                if (cleanedRegionMap.containsKey(regionIdStr)) {
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
            if (cleanedRegionMap.containsKey(exonIdStr)) {
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
                SortedRegion sortedExon = cleanedRegionMap.get(regionIdStr);
                if (sortedExon != null) {
                    geneSize++;
                    caseAvg += sortedExon.getCaseAvg();
                    ctrlAvg += sortedExon.getCtrlAvg();
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
            SortedRegion sortedExon = cleanedRegionMap.get(regionIdStr);
            if (sortedExon != null) {
                geneSize += sortedExon.getLength();
                caseAvg += (double) sortedExon.getLength() * sortedExon.getCaseAvg();
                ctrlAvg += (double) sortedExon.getLength() * sortedExon.getCtrlAvg();
            }
        }

        return getGeneStr(gene, geneSize, caseAvg, ctrlAvg);
    }

    private String getGeneStr(Gene gene, int geneSize, double caseAvg, double ctrlAvg) {
        StringBuilder sb = new StringBuilder();

        sb.append(gene.getName()).append(",");
        sb.append(gene.getChr()).append(",");
        sb.append(gene.getLength()).append(",");
        caseAvg = MathManager.devide(caseAvg, geneSize);
        ctrlAvg = MathManager.devide(ctrlAvg, geneSize);
        sb.append(FormatManager.getSixDegitDouble(caseAvg)).append(",");
        sb.append(FormatManager.getSixDegitDouble(ctrlAvg)).append(",");
        double absDiff = MathManager.abs(caseAvg, ctrlAvg);
        sb.append(FormatManager.getSixDegitDouble(absDiff)).append(",");
        sb.append(geneSize).append(",");
        sb.append(CoverageCommand.checkGeneCleanCutoff(absDiff, caseAvg, ctrlAvg));
        return sb.toString();
    }

    public void outputLog() {
        int numExonsTotal = regionList.size();
        int numExonsPruned = numExonsTotal - cleanedRegionMap.size();
        LogManager.writeAndPrint("The number of exons before pruning is " + numExonsTotal);
        LogManager.writeAndPrint("The number of exons after pruning is " + cleanedRegionMap.size());
        LogManager.writeAndPrint("The number of exons pruned is " + numExonsPruned);
        double percentExonsPruned = (double) numExonsPruned / (double) numExonsTotal * 100;
        LogManager.writeAndPrint("The % of exons pruned is "
                + FormatManager.getSixDegitDouble(percentExonsPruned) + "%");

        LogManager.writeAndPrint("The total number of bases before pruning is "
                + FormatManager.getSixDegitDouble((double) totalBases / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is "
                + FormatManager.getSixDegitDouble((double) totalCleanedBases / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is "
                + FormatManager.getSixDegitDouble(100.0 - (double) totalCleanedBases / (double) totalBases * 100) + "%");
        
        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is "
                + FormatManager.getSixDegitDouble(getAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is "
                + FormatManager.getSixDegitDouble(getAllCoverage() * totalBases / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for cases after pruning is  "
                + FormatManager.getSixDegitDouble(caseCoverage * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for cases after pruning is "
                + FormatManager.getSixDegitDouble(caseCoverage * totalBases / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for controls after pruning is  "
                + FormatManager.getSixDegitDouble(ctrlCoverage * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for controls after pruning is "
                + FormatManager.getSixDegitDouble(ctrlCoverage * totalBases / 1000000.0) + " MB");
    }
}
