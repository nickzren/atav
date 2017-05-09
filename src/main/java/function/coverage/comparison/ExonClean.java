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
public class ExonClean {

    int totalBases = 0;
    int totalCleanedBases = 0;
    double caseCoverage = 0;
    double ctrlCoverage = 0;
    ArrayList<SortedExon> exonList = new ArrayList<>();
    HashMap<String, SortedExon> cleanedExonMap = new HashMap<>();

    public void addExon(String name, float caseAvg, float ctrlAvg, float covDiff, int regionSize) {
        exonList.add(new SortedExon(name, caseAvg, ctrlAvg, covDiff, regionSize));
        totalBases += regionSize;
    }

    private double getAllCoverage() {
        return (caseCoverage * SampleManager.getCaseNum() + ctrlCoverage * SampleManager.getCtrlNum()) / SampleManager.getListSize();
    }

    protected double getCutoff() {
        //make sure the list has included all data and sortd.
        Collections.sort(exonList);

        int i;
        float cutoff;
        float[] data = new float[exonList.size()];
        float meandata = 0;
        //calculate mean data
        for (i = 0; i < data.length; i++) {
            data[i] = exonList.get(i).getCovDiff();
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

        cutoff = exonList.get(index).getCovDiff();

        LogManager.writeAndPrint("\nThe automated cutoff value for absolute mean coverage difference for sites is " + Float.toString(cutoff));

        if (CoverageCommand.exonCleanCutoff != Data.NO_FILTER) {
            cutoff = CoverageCommand.exonCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value "
                    + FormatManager.getSixDegitDouble(cutoff) + " is applied instead.");
        }

        return cutoff;

    }

    public void initCleanedRegionMap() {
        double cutoff = getCutoff();

        for (SortedExon sortedRegion : exonList) {
            if (sortedRegion.getCutoff() < cutoff
                    && sortedRegion.getCaseAvg() + sortedRegion.getCtrlAvg() > 0) {
                totalCleanedBases += sortedRegion.getLength();
                ctrlCoverage += sortedRegion.getCtrlAvg() * sortedRegion.getLength();
                caseCoverage += sortedRegion.getCaseAvg() * sortedRegion.getLength();

                cleanedExonMap.put(sortedRegion.getName(), sortedRegion);
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
            String exonIdStr = gene.getName() + "_" + exon.getIdStr();
            if (cleanedExonMap.containsKey(exonIdStr)) {
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
        float caseAvg = 0;
        float ctrlAvg = 0;
        for (Exon exon : gene.getExonList()) {
            String regionIdStr = gene.getName() + "_" + exon.getIdStr();
            SortedExon sortedExon = cleanedExonMap.get(regionIdStr);
            if (sortedExon != null) {
                geneSize += sortedExon.getLength();
                caseAvg += (float) sortedExon.getLength() * sortedExon.getCaseAvg();
                ctrlAvg += (float) sortedExon.getLength() * sortedExon.getCtrlAvg();
            }
        }

        return getGeneStr(gene, geneSize, caseAvg, ctrlAvg);
    }

    private String getGeneStr(Gene gene, int geneSize, float caseAvg, float ctrlAvg) {
        StringBuilder sb = new StringBuilder();

        sb.append(gene.getName()).append(",");
        sb.append(gene.getChr()).append(",");
        sb.append(gene.getLength()).append(",");
        caseAvg = MathManager.devide(caseAvg, geneSize);
        ctrlAvg = MathManager.devide(ctrlAvg, geneSize);
        sb.append(FormatManager.getFloat(caseAvg)).append(",");
        sb.append(FormatManager.getFloat(ctrlAvg)).append(",");
        
        float covDiff = Data.NA;

        if (CoverageCommand.isRelativeDifference) {
            covDiff = MathManager.relativeDiff(caseAvg, ctrlAvg);
        } else {
            covDiff = MathManager.abs(caseAvg, ctrlAvg);
        }

        sb.append(FormatManager.getFloat(covDiff)).append(",");
        sb.append(geneSize);
        return sb.toString();
    }

    public void outputLog() {
        int numExonsTotal = exonList.size();
        int numExonsPruned = numExonsTotal - cleanedExonMap.size();
        LogManager.writeAndPrint("The number of exons before pruning is " + numExonsTotal);
        LogManager.writeAndPrint("The number of exons after pruning is " + cleanedExonMap.size());
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
