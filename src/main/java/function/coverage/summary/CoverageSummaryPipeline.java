package function.coverage.summary;

import function.coverage.base.CoverageCommand;
import function.coverage.base.SampleStatistics;
import function.coverage.base.Exon;
import function.coverage.base.Gene;
import function.coverage.base.InputList;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author qwang
 */
public class CoverageSummaryPipeline extends InputList {

    public CoverageSummaryPipeline() {
        super();
        try {
            if (CommonCommand.minCoverage == Data.NO_FILTER) {
                ErrorManager.print("--min-coverage option has to be used in this function.");
            }

            if (!CoverageCommand.coveredRegionFile.contains("/nfs/goldstein/software/atav_home/data")) {
                ErrorManager.print("System gene index file has to be used to run this command.");
            }
            BufferedReader br = new BufferedReader(new FileReader(CoverageCommand.coveredRegionFile));
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
        int i;
        int[] MinCoverages = {3, 10, 20};
        ArrayList<SampleStatistics> ssList = new ArrayList<SampleStatistics>();
        for (i = 0; i < MinCoverages.length; i++) {
            ssList.add(new SampleStatistics(size()));
        }
        for (Iterator it = this.iterator(); it.hasNext();) {
            int record = ssList.get(0).getNextRecord();

            Object obj = it.next();
            if (!CoverageCommand.isTerse) {
                System.out.print("Processing " + (record + 1) + " of " + size() + ":        " + obj.toString() + "                              \r");
            }
            Gene gene = (Gene) obj;
            gene.populateSlaveList();
            for (i = 0; i < MinCoverages.length; i++) {
                ssList.get(i).setRecordName(record, gene.getName(), gene.getChr());
                ssList.get(i).setLength(record, gene.getLength());
            }

            for (Iterator r = gene.getExonList().iterator(); r.hasNext();) {
                Exon exon = (Exon) r.next();
                ArrayList<HashMap<Integer, Integer>> result = exon.getCoverage(MinCoverages);
                for (i = 0; i < MinCoverages.length; i++) {
                    ssList.get(i).accumulateCoverage(record, result.get(i));
                }
                result.clear();
            }
            LogManager.writeLog("Gene ENSTID: " + gene.getName() + "\t" + "CONSENOUS_TRANSCRIPT" + "\t(" + (record + 1) + " of " + size() + ")");

            for (i = 0; i < MinCoverages.length; i++) {
                ssList.get(i).updateMatrixRowInDataBase(record);
            }
        }
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
