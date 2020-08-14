package function.coverage.comparison;

import function.annotation.base.Exon;
import function.annotation.base.Gene;
import function.annotation.base.GeneManager;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.coverage.base.CoverageCommand;
import function.cohort.base.SampleManager;
import global.Data;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.FormatManager;
import utils.LogManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class SiteClean {

    int totalCleanedBases = 0;
    int zeroIndividualPassMinCovSiteCount = 0;
    double caseCoverage = 0;
    double ctrlCoverage = 0;
    ArrayList<SortedSite> siteList = new ArrayList<>();
    HashMap<String, HashMap<Integer, SortedSite>> cleanedSiteMap = new HashMap<>();

    public void addSite(String chr, int pos, float caseAvg, float ctrlAvg, float covDiff) {
        siteList.add(new SortedSite(chr, pos, caseAvg, ctrlAvg, covDiff));
    }

    private double getAllCoverage() {
        return (caseCoverage * SampleManager.getCaseNum() + ctrlCoverage * SampleManager.getCtrlNum()) / SampleManager.getTotalSampleNum();
    }

    protected double getCutoff() {
        //make sure the list has included all data and sortd.
        Collections.sort(siteList);

        int i;
        float cutoff;
        float[] data = new float[siteList.size()];
        float meandata = 0;
        //calculate mean data
        for (i = 0; i < data.length; i++) {
            data[i] = siteList.get(i).getCovDiff();
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

        cutoff = siteList.get(index).getCovDiff();

        LogManager.writeAndPrint("\nThe automated cutoff value for absolute mean coverage difference for sites is " + Float.toString(cutoff));

        if (CoverageCommand.siteCleanCutoff != Data.NO_FILTER) {
            cutoff = CoverageCommand.siteCleanCutoff;
            LogManager.writeAndPrint("User specified cutoff value " + FormatManager.getFloat(cutoff) + " is applied instead.");
        }

        return cutoff;

    }

    public void initCleanedSiteMap() {
        double cutoff = getCutoff();

        for (SortedSite sortedSite : siteList) {
            if (sortedSite.getCutoff() < cutoff) {
                if (sortedSite.getCaseAvg() + sortedSite.getCtrlAvg() > 0) {
                    totalCleanedBases++;
                    ctrlCoverage += sortedSite.getCtrlAvg();
                    caseCoverage += sortedSite.getCaseAvg();

                    HashMap<Integer, SortedSite> map = cleanedSiteMap.get(sortedSite.getChr());

                    if (map == null) {
                        map = new HashMap<>();
                        cleanedSiteMap.put(sortedSite.getChr(), map);
                    }

                    map.put(sortedSite.getPos(), sortedSite);
                } else {
                    zeroIndividualPassMinCovSiteCount++;
                }
            }
        }

        caseCoverage = MathManager.devide(caseCoverage, GeneManager.getAllGeneBoundaryLength());
        ctrlCoverage = MathManager.devide(ctrlCoverage, GeneManager.getAllGeneBoundaryLength());

        siteList.clear(); // free memory
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

            int previouStartPosition = Data.INTEGER_NA;
            int previousEndPosition = Data.INTEGER_NA;
            for (int currentPosition = start; currentPosition <= end; currentPosition++) {
                HashMap<Integer, SortedSite> map = cleanedSiteMap.get(gene.getChr());

                if (map != null
                        && map.containsKey(currentPosition)) {
                    if (previouStartPosition == Data.INTEGER_NA) {
                        previouStartPosition = currentPosition;
                    }
                    size += 1;
                    previousEndPosition = currentPosition;
                } else //there are gaps within the exon, so record the previos discovered region if any
                //need to record the new discovered region
                {
                    if (previouStartPosition != Data.INTEGER_NA) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            sb.append(",");
                        }
                        sb.append(previouStartPosition);
                        sb.append("..").append(previousEndPosition);
                        //reset to no region
                        previouStartPosition = Data.INTEGER_NA;
                        previousEndPosition = Data.INTEGER_NA;
                    }
                }
            }
            //repeat the recoring for last potential region
            //make sure this code is consistent with the code in previous section
            if (previouStartPosition != Data.INTEGER_NA) {
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

    public String getCleanedGeneSummaryStrBySite(Gene gene) {
        int geneSize = 0;
        float caseAvg = 0;
        float ctrlAvg = 0;
        for (Exon exon : gene.getExonList()) {
            int start = exon.getStartPosition();
            int end = exon.getEndPosition();
            for (int currentPosition = start; currentPosition <= end; currentPosition++) {
                HashMap<Integer, SortedSite> sortedSiteMap = cleanedSiteMap.get(gene.getChr());

                if (sortedSiteMap != null) {
                    SortedSite sortedSite = sortedSiteMap.get(currentPosition);
                    if (sortedSite != null) {
                        geneSize++;
                        caseAvg += sortedSite.getCaseAvg();
                        ctrlAvg += sortedSite.getCtrlAvg();
                    }
                }
            }
        }

        return getGeneStr(gene, geneSize, caseAvg, ctrlAvg);
    }

    private String getGeneStr(Gene gene, int geneSize, float caseAvg, float ctrlAvg) {
        caseAvg = MathManager.devide(caseAvg, geneSize);
        ctrlAvg = MathManager.devide(ctrlAvg, geneSize);

        StringJoiner sj = new StringJoiner(",");

        sj.add(gene.getName());
        sj.add(gene.getChr());
        sj.add(FormatManager.getInteger(gene.getLength()));
        sj.add(FormatManager.getFloat(caseAvg));
        sj.add(FormatManager.getFloat(ctrlAvg));

        float covDiff = Data.FLOAT_NA;

        if (CoverageCommand.isRelativeDifference) {
            covDiff = MathManager.relativeDiff(caseAvg, ctrlAvg);
        } else {
            covDiff = MathManager.abs(caseAvg, ctrlAvg);
        }

        sj.add(FormatManager.getFloat(covDiff));
        sj.add(FormatManager.getInteger(geneSize));
        return sj.toString();
    }

    public void outputLog() {
        LogManager.writeAndPrint("The total number of bases before pruning is "
                + FormatManager.getDouble((double) GeneManager.getAllGeneBoundaryLength() / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases with zero individuals passing min-coverage " + GenotypeLevelFilterCommand.minDpBin + " is "
                + FormatManager.getDouble((double) zeroIndividualPassMinCovSiteCount / 1000000.0) + " MB");
        LogManager.writeAndPrint("The total number of bases after pruning is "
                + FormatManager.getDouble((double) totalCleanedBases / 1000000.0) + " MB");
        LogManager.writeAndPrint("The % of bases with zero individuals passing min-coverage min-coverage " + GenotypeLevelFilterCommand.minDpBin + " is "
                + FormatManager.getDouble((double) zeroIndividualPassMinCovSiteCount / (double) GeneManager.getAllGeneBoundaryLength() * 100) + "%");
        LogManager.writeAndPrint("The % of bases pruned is "
                + FormatManager.getDouble(100.0 - (double) totalCleanedBases / (double) GeneManager.getAllGeneBoundaryLength() * 100) + "%");

        LogManager.writeAndPrint("The average coverage rate for all samples after pruning is "
                + FormatManager.getDouble(getAllCoverage() * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for all samples after pruning is "
                + FormatManager.getDouble(getAllCoverage() * GeneManager.getAllGeneBoundaryLength() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for cases after pruning is  "
                + FormatManager.getDouble(caseCoverage * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for cases after pruning is "
                + FormatManager.getDouble(caseCoverage * GeneManager.getAllGeneBoundaryLength() / 1000000.0) + " MB");

        LogManager.writeAndPrint("The average coverage rate for controls after pruning is  "
                + FormatManager.getDouble(ctrlCoverage * 100) + "%");
        LogManager.writeAndPrint("The average number of bases well covered for controls after pruning is "
                + FormatManager.getDouble(ctrlCoverage * GeneManager.getAllGeneBoundaryLength() / 1000000.0) + " MB");
    }
}
