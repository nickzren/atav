package function.coverage.summary;

import function.AnalysisBase;
import function.annotation.base.GeneManager;
import function.coverage.base.CoverageCommand;
import function.coverage.base.CoverageManager;
import function.coverage.base.SampleStatistics;
import function.annotation.base.Exon;
import function.annotation.base.Gene;
import utils.CommonCommand;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import utils.ErrorManager;

/**
 *
 * @author qwang
 */
public class SiteCoverageSummary extends AnalysisBase {

    BufferedWriter bwSiteSummary = null;
    public final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";

    @Override
    public void initOutput() {
        try {
            bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
            bwSiteSummary.write("Gene,Chr,Pos,Site Coverage");
            
            if (CoverageCommand.isCaseControlSeparate) {
                bwSiteSummary.write(",Site Coverage Case, Site Coverage Control");
            }
            bwSiteSummary.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwSiteSummary.flush();
            bwSiteSummary.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {
        try {
            SampleStatistics ss = new SampleStatistics(GeneManager.getGeneBoundaryList().size());

            int record = 0;

            for (Gene gene : GeneManager.getGeneBoundaryList()) {
                System.out.print("Processing " + (record + 1) + " of " + GeneManager.getGeneBoundaryList().size() + ": " + gene.toString() + "                              \r");

                ss.setRecordName(record, gene.getName(), gene.getChr());
                ss.setLength(record, gene.getLength());

                for (Exon exon : gene.getExonList()) {
                    emitExoninfo(ss, exon, record);

                    String chr = exon.getChrStr();
                    int SiteStart = exon.getStartPosition();

                    ArrayList<int[]> SiteCoverage = CoverageManager.getCoverageForSites(exon);

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
        } catch (Exception e) {
            ErrorManager.send(e);
        }
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

    @Override
    public String toString() {
        return "It is running site coverage summary function...";
    }
}
