package function.coverage.summary;

import function.coverage.base.CoverageCommand;
import function.coverage.base.CoverageManager;
import function.coverage.base.CoveredRegion;
import function.coverage.base.SampleStatistics;
import function.coverage.base.Exon;
import function.coverage.base.Gene;
import function.coverage.base.InputList;
import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author qwang
 */
public class SiteCoverageSummary extends InputList {

    BufferedWriter bwSiteSummary = null;
    public final String siteSummaryFilePath = CommonCommand.outputPath + "site.summary.csv";
    
    public SiteCoverageSummary() {
        super();

        try {
            if (GenotypeLevelFilterCommand.minCoverage == Data.NO_FILTER) {
                ErrorManager.print("--min-coverage option has to be used in this function.");
            }
            
            BufferedReader br = new BufferedReader(new FileReader(CoverageCommand.coveredRegionFile));
            String str;
            while ((str = br.readLine()) != null && str.length() > 0) {
                try {
                    Gene gene = new Gene(str);
                    if (gene.isValid()) {
                        add(gene);
                    }

                } catch (NumberFormatException e) {
                    LogManager.writeAndPrint("Invalid region format: " + str);
                }
            }
            br.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void run() throws Exception {
        initOutput();
        SampleStatistics ss = new SampleStatistics(size());

        int record = 0;
        
        for (Iterator it = this.iterator(); it.hasNext();) {
            Object obj = it.next();
            ss.setRecordName(record, obj.toString(),"");

            String JobType = obj.getClass().getSimpleName();

            if (JobType.equals("Gene")) {
                Gene gene = (Gene) obj;
                if (gene.getType().equalsIgnoreCase("boundary")) {
                    gene.populateSlaveList();
                    ss.setRecordName(record, gene.getName(),gene.getChr());
                    ss.setLength(record, gene.getLength());

                    for (Iterator r = gene.getExonList().iterator(); r.hasNext();) {
                        Exon exon = (Exon) r.next();
                        emitExoninfo(ss,exon,record);
                      
                        CoveredRegion cr = exon.getCoveredRegion();
                        String chr = cr.getChrStr();
                        int SiteStart = cr.getStartPosition();
                        
                        ArrayList<int[]> SiteCoverage =  CoverageManager.getCoverageForSites(GenotypeLevelFilterCommand.minCoverage, cr);
                        
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
                }
                DoGeneSummary(ss, record);

                LogManager.writeLog("Gene: " + gene.getName() + "\t(" + (record + 1) + " of " + size() + ")");
            }
            
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
