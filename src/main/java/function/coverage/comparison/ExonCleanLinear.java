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
public class ExonCleanLinear {

    int totalBases = 0;
    int totalCleanedBases = 0;
    double caseCoverage = 0;
    double ctrlCoverage = 0;
    ArrayList<SortedExonLinear> regionList = new ArrayList<>();
    HashMap<String, SortedExonLinear> cleanedRegionMap = new HashMap<>();

    public void addExon(String name, float caseAvg, float ctrlAvg, float covDiff, int regionSize,
            double p, double r2, double variance) {
        regionList.add(new SortedExonLinear(name, caseAvg, ctrlAvg, covDiff, regionSize,
                p, r2, variance));

        totalBases += regionSize;
    }

    private double getAllCoverage() {
        return (caseCoverage * SampleManager.getCaseNum() + ctrlCoverage * SampleManager.getCtrlNum()) / SampleManager.getListSize();
    }

    protected double getCutoff() {
        Collections.sort(regionList);

        int i;
        double cutoff;
        double[] data = new double[regionList.size()];

        double total_variation = 0.0;
        for (i = 0; i < data.length; i++) {
            data[i] = regionList.get(i).getVariant();
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

        cutoff = regionList.get(index).getVariant();

        LogManager.writeAndPrint("\nThe automated cutoff value for variance for exons is " + Double.toString(cutoff));

        if (CoverageCommand.exonCleanCutoff != Data.NO_FILTER) {
            cutoff = CoverageCommand.exonCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value "
                    + FormatManager.getDouble(cutoff) + " is applied instead.");
        }

        return cutoff;
    }

    public void initCleanedRegionMap() {
        double cutoff = getCutoff();

        for (SortedExonLinear sortedRegion : regionList) {
            if (sortedRegion.getCutoff() < cutoff) {
                totalCleanedBases += sortedRegion.getLength();
                ctrlCoverage += sortedRegion.getCtrlAvg() * sortedRegion.getLength();
                caseCoverage += sortedRegion.getCaseAvg() * sortedRegion.getLength();

                cleanedRegionMap.put(sortedRegion.getName(), sortedRegion);
            }
        }

        caseCoverage = MathManager.devide(caseCoverage, totalBases);
        ctrlCoverage = MathManager.devide(ctrlCoverage, totalBases);
    }

    public String getCleanedGeneStrByExon(Gene gene) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        sb.append(gene.getName());
        sb.append(" ").append(gene.getChr()).append(" (");
        boolean isFirst = true;
        for (Exon exon : gene.getExonList()) {
            String regionIdStr = gene.getName() + "_" + exon.getId();
            if (cleanedRegionMap.containsKey(regionIdStr)) {
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
        float avgCase = 0;
        float avgCtrl = 0;
        for (Exon exon : gene.getExonList()) {
            String regionId = gene.getName() + "_" + exon.getId();

            SortedExonLinear sortedExon = cleanedRegionMap.get(regionId);
            if (sortedExon != null) {
                geneSize += sortedExon.getLength();
                avgCase += (float) sortedExon.getLength() * sortedExon.getCaseAvg();
                avgCtrl += (float) sortedExon.getLength() * sortedExon.getCtrlAvg();
            }
        }

        return getGeneStr(gene, geneSize, avgCase, avgCtrl);
    }

    private String getGeneStr(Gene gene, int geneSize, float caseAvg, float ctrlAvg) {
        caseAvg = MathManager.devide(caseAvg, geneSize);
        ctrlAvg = MathManager.devide(ctrlAvg, geneSize);
        
        if (CoverageCommand.isMinCoverageFractionValid(caseAvg)
                && CoverageCommand.isMinCoverageFractionValid(ctrlAvg)) {
            StringBuilder sb = new StringBuilder();

            sb.append(gene.getName()).append(",");
            sb.append(gene.getChr()).append(",");
            sb.append(gene.getLength()).append(",");
            sb.append(FormatManager.getFloat(caseAvg)).append(",");
            sb.append(FormatManager.getFloat(ctrlAvg)).append(",");

            float covDiff = Data.FLOAT_NA;

            if (CoverageCommand.isRelativeDifference) {
                covDiff = MathManager.relativeDiff(caseAvg, ctrlAvg);
            } else {
                covDiff = MathManager.abs(caseAvg, ctrlAvg);
            }

            sb.append(FormatManager.getFloat(covDiff)).append(",");
            sb.append(geneSize);
            return sb.toString();
        } else {
            return "";
        }
    }

    public void outputLog() {
        int numExonsTotal = regionList.size();;
        int numExonsPruned = numExonsTotal - cleanedRegionMap.size();

        LogManager.writeAndPrint("The number of exons before pruning is " + numExonsTotal);
        LogManager.writeAndPrint("The number of exons after pruning is " + cleanedRegionMap.size());
        LogManager.writeAndPrint("The number of exons pruned is " + numExonsPruned);
        double percentExonsPruned = (double) numExonsPruned / (double) numExonsTotal * 100;
        LogManager.writeAndPrint("The % of exons pruned is "
                + FormatManager.getDouble(percentExonsPruned) + "%");

        LogManager.writeAndPrint("The total number of bases before pruning is "
                + FormatManager.getDouble((double) totalBases / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is "
                + FormatManager.getDouble((double) totalCleanedBases / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases pruned is "
                + FormatManager.getDouble(100.0 - (double) totalCleanedBases / (double) totalBases * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is  "
                + FormatManager.getDouble(getAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is  "
                + FormatManager.getDouble(getAllCoverage() * totalBases / 1000000.0) + " MB");
    }
}
