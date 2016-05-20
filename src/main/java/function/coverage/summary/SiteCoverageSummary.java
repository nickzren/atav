package function.coverage.summary;

import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.coverage.base.CoverageManager;
import function.coverage.base.SampleStatistics;
import function.coverage.base.Exon;
import function.coverage.base.Gene;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.variant.base.Region;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 * @author qwang
 */
public class SiteCoverageSummary {

    BufferedWriter bwSiteSummary = null;
    public final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";

    public SiteCoverageSummary() {
        super();

        try {
            if (GenotypeLevelFilterCommand.minCoverage == Data.NO_FILTER) {
                ErrorManager.print("--min-coverage option has to be used in this function.");
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void run() throws Exception {
        initOutput();
        SampleStatistics ss = new SampleStatistics(GeneManager.getGeneBoundaryList().size());

        int record = 0;

        for (Gene gene : GeneManager.getGeneBoundaryList()) {
            System.out.print("Processing " + (record + 1) + " of " + GeneManager.getGeneBoundaryList().size() + ":        " + gene.toString() + "                              \r");

            gene.initExonList();
            ss.setRecordName(record, gene.getName(), gene.getChr());
            ss.setLength(record, gene.getLength());

            for (Exon exon : gene.getExonList()) {
                emitExoninfo(ss, exon, record);

                Region cr = exon.getRegion();
                String chr = cr.getChrStr();
                int SiteStart = cr.getStartPosition();

                ArrayList<int[]> SiteCoverage = CoverageManager.getCoverageForSites(GenotypeLevelFilterCommand.minCoverage, cr);

                for (int pos = 0; pos < SiteCoverage.get(0).length; pos++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(gene.getName()).append(",").append(chr).append(",");
                    int total_coverage = SiteCoverage.get(0)[pos] + SiteCoverage.get(1)[pos];
                    sb.append(SiteStart + pos).append(",").append(total_coverage);
                    if (CoverageCommand.isCaseControlSeparate) {
                        sb.append(",").append(SiteCoverage.get(0)[pos]);
                        sb.append(",").append(SiteCoverage.get(1)[pos]);
                        //emit site info for potential processing
                        emitSiteInfo(gene.getName(), chr, SiteStart + pos,
                                SiteCoverage.get(0)[pos], SiteCoverage.get(1)[pos]);
                    }
                    sb.append("\n");
                    bwSiteSummary.write(sb.toString());
                }
            }

            DoGeneSummary(ss, record);
            record++;
        }

        emitSS(ss);
        closeOutput();
    }

    public void emitSS(SampleStatistics ss) {
        //allow derived class to peek into SampleStatistics
    }

    public void emitExoninfo(SampleStatistics ss, Exon exon, int record) {
        //allow derived class to do extra on an exon
    }

    public void DoGeneSummary(SampleStatistics ss, int record) throws Exception {
        //do nothing for coverage summary
    }

    //give a chance for derived class to process a site
    public void emitSiteInfo(String gene, String chr, int position, int caseCoverage, int ctrlCoverage) {

    }

    private void initOutput() throws Exception {
        bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
        bwSiteSummary.write("Gene,Chr,Pos,Site Coverage");
        if (CoverageCommand.isCaseControlSeparate) {
            bwSiteSummary.write(",Site Coverage Case, Site Coverage Control");
        }
        bwSiteSummary.newLine();
    }

    private void closeOutput() throws Exception {
        bwSiteSummary.flush();
        bwSiteSummary.close();
    }
}
