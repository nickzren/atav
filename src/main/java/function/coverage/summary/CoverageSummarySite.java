package function.coverage.summary;

import function.coverage.base.CoveredRegion;
import function.coverage.base.SampleStatistics;
import function.coverage.base.Exon;
import function.coverage.base.Gene;
import function.coverage.base.InputList;
import global.Data;
import function.genotype.base.SampleManager;
import utils.CommandValue;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;

/**
 *
 * @author qwang
 */
public class CoverageSummarySite extends InputList {

    BufferedWriter bwSiteSummary = null;
    final String siteSummaryFilePath = CommandValue.outputPath + "site.summary.csv";

    public CoverageSummarySite() {
        super();

        try {
            if (CommandValue.minCoverage == Data.NO_FILTER) {
                ErrorManager.print("--min-coverage option has to be used in this function.");
            }
            
            BufferedReader br = new BufferedReader(new FileReader(CommandValue.coveredRegionFile));
            String str;
            int LineCount = 0;
            while ((str = br.readLine()) != null && str.length() > 0) {
                try {
                    Gene gene = new Gene(str);
                    if (gene.isValid()) {
                        add(gene);
                    }

                } catch (NumberFormatException e) {
                    LogManager.writeAndPrint("Invalid region format: " + str);
                }
                LineCount++;
            }
            br.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void run() throws Exception {
        initOutput();
        String strSamples = SampleManager.getAllSampleId();
        SampleStatistics ss = new SampleStatistics(strSamples, size());

        for (Iterator it = this.iterator(); it.hasNext();) {
            int record = ss.getNextRecord();

            Object obj = it.next();
            ss.setRecordName(record, obj.toString(),"");

            String JobType = obj.getClass().getSimpleName();

            if (JobType.equals("Gene")) {
                Gene gene = (Gene) obj;
                if (gene.getType().equalsIgnoreCase("Slave")) {
                    gene.populateSlaveList();
                    ss.setRecordName(record, gene.getName(),gene.getChr());
                    ss.setLength(record, gene.getLength());

                    for (Iterator r = gene.getExonList().iterator(); r.hasNext();) {
                        CoveredRegion cr = ((Exon) r.next()).getCoveredRegion();
                        String chr = cr.getChrStr();
                        int SiteStart = cr.getStartPosition(); 
                        int[] SiteCoverage = cr.getCoverageForSites(CommandValue.minCoverage);
                        for (int pos = 0; pos < SiteCoverage.length; pos++) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(gene.getName()).append(",").append(chr).append(",");
                            sb.append(SiteStart + pos).append(",").append(SiteCoverage[pos]).append("\n");
                            bwSiteSummary.write(sb.toString());
                        }
                    }
                }

                LogManager.writeLog("Gene: " + gene.getName() + "\t(" + (record + 1) + " of " + size() + ")");
            }
        }
        closeOutput();
    }

    private void initOutput() throws Exception {
        bwSiteSummary = new BufferedWriter(new FileWriter(siteSummaryFilePath));
        bwSiteSummary.write("Gene,Chr,Pos,Site Coverage");
        bwSiteSummary.newLine();
    }

    private void closeOutput() throws Exception {
        bwSiteSummary.flush();
        bwSiteSummary.close();

    }

    @Override
    public String toString() { //for debug purpose, echo the input genes/transcripts/regions
        StringBuilder str = new StringBuilder();
        if (!this.isEmpty()) {
            for (Iterator it = this.iterator(); it.hasNext();) {
                Object obj = it.next();
                String JobType = obj.getClass().getSimpleName();
                str.append(JobType + ": " + obj.toString()).append("\n");
            }
        } else {
            str.append("Warning: no valid regions/genes/transcripts found!");
        }
        return str.toString();
    }
}
