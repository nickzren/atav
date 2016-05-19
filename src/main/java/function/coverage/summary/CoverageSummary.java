package function.coverage.summary;

import function.coverage.base.CoverageCommand;
import function.coverage.base.CoverageManager;
import function.coverage.base.SampleStatistics;
import function.coverage.base.CoveredRegion;
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
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author qwang
 */
public class CoverageSummary extends InputList {

    public BufferedWriter bwSampleSummary = null;
    public BufferedWriter bwSampleRegionSummary = null;
    public BufferedWriter bwSampleMatrixSummary = null;
    public BufferedWriter bwSampleExonMatrixSummary = null;
    public BufferedWriter bwCoverageDetailsByExon = null;
    public BufferedWriter bwCoverageSummaryByExon = null;
    public BufferedWriter bwCoverageSummaryByGene = null;

    public final String sampleSummaryFilePath = CommonCommand.outputPath + "sample.summary.csv";
    public final String coverageDetailsFilePath = CommonCommand.outputPath + "coverage.details.csv";
    public final String coverageDetailsByExonFilePath = CommonCommand.outputPath + "coverage.details.by.exon.csv";
    public final String coverageMatrixFilePath = CommonCommand.outputPath + "coverage.details.matrix.csv";
    public final String coverageExonMatrixFilePath = CommonCommand.outputPath + "coverage.details.matrix.by.exon.csv";

    public CoverageSummary() {
        super();

        if (GenotypeLevelFilterCommand.minCoverage == Data.NO_FILTER) {
            ErrorManager.print("--min-coverage option has to be used in this function.");
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(CoverageCommand.coveredRegionFile));
            String str;
            while ((str = br.readLine()) != null && str.length() > 0) {
                try {
                    if (!addRegion(str)) {
                        Gene gene = new Gene(str);
                        if (gene.isValid()) {
                            add(gene);
                        }
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

    public void DoExonSummary(SampleStatistics ss, int record, HashMap<Integer, Integer> result, Exon e) throws Exception {
        ss.print(record, result, e, bwCoverageDetailsByExon);
    }

    public void DoGeneSummary(SampleStatistics ss, int record) throws Exception {
        //do nothing for coverage summary
    }

    public void run() throws Exception {
        initOutput();
        SampleStatistics ss = new SampleStatistics(size());
        ss.printMatrixHeader(bwSampleMatrixSummary, false);

        if (CoverageCommand.isByExon) {
            ss.printMatrixHeader(bwSampleExonMatrixSummary, true);
        }

        int record = 0;
        
        for (Iterator it = this.iterator(); it.hasNext();) {
            Object obj = it.next();

            System.out.print("Processing " + (record + 1) + " of " + size() + ":        " + obj.toString() + "                              \r");

            String JobType = obj.getClass().getSimpleName();
            //the following should be simplified by implementing a same interface. Q.
            if (JobType.contains("CoveredRegion")) {
                CoveredRegion region = (CoveredRegion) obj;
                ss.setRecordName(record, region.toString(), region.getChrStr());
                ss.setLength(record, region.getLength());
                int[] mincovs = {GenotypeLevelFilterCommand.minCoverage};
                HashMap<Integer, Integer> result = CoverageManager.getCoverage(mincovs, region).get(0);
                ss.accumulateCoverage(record, result);
            } else if (JobType.equals("Gene")) {
                Gene gene = (Gene) obj;
                
                gene.populateSlaveList();
                ss.setRecordName(record, gene.getName(), gene.getChr());
                ss.setLength(record, gene.getLength());

                for (Iterator r = gene.getExonList().iterator(); r.hasNext();) {
                    Exon exon = (Exon) r.next();
                    HashMap<Integer, Integer> result = exon.getCoverage(GenotypeLevelFilterCommand.minCoverage);
                    ss.accumulateCoverage(record, result);
                    ss.printMatrixRowbyExon(record, result, exon, bwSampleExonMatrixSummary);
                    DoExonSummary(ss, record, result, exon);
                }
            }

            ss.print(record, bwSampleRegionSummary);
            ss.printMatrixRow(record, bwSampleMatrixSummary);
            DoGeneSummary(ss, record);
            
            record++;
        }

        ss.print(bwSampleSummary);
        closeOutput();
    }

    public void initOutput() throws Exception {
        bwSampleSummary = new BufferedWriter(new FileWriter(sampleSummaryFilePath));
        bwSampleSummary.write("Sample,Total_Bases,Total_Covered_Base,%Overall_Bases_Covered,"
                + "Total_Regions,Total_Covered_Regions,%Regions_Covered");
        bwSampleSummary.newLine();

        bwSampleRegionSummary = new BufferedWriter(new FileWriter(coverageDetailsFilePath));
        bwSampleRegionSummary.write("Sample,Gene/Transcript/Region,Chr,Length,"
                + "Covered_Base,%Bases_Covered,Coverage_Status");
        bwSampleRegionSummary.newLine();

        bwSampleMatrixSummary = new BufferedWriter(new FileWriter(coverageMatrixFilePath));

        if (CoverageCommand.isByExon) {
            if (CoverageCommand.isCoverageSummary) {
                bwCoverageDetailsByExon = new BufferedWriter(new FileWriter(coverageDetailsByExonFilePath));
                bwCoverageDetailsByExon.write("Sample,Gene/Transcript,Chr,Exon,Start_Position, Stop_Position,Length,Covered_Base,%Bases_Covered,Coverage_Status");
                bwCoverageDetailsByExon.newLine();
            }
            bwSampleExonMatrixSummary = new BufferedWriter(new FileWriter(coverageExonMatrixFilePath));
        }
    }

    public void closeOutput() throws Exception {
        bwSampleSummary.flush();
        bwSampleSummary.close();
        bwSampleRegionSummary.flush();
        bwSampleRegionSummary.close();
        bwSampleMatrixSummary.flush();
        bwSampleMatrixSummary.close();

        if (CoverageCommand.isByExon) {
            if (CoverageCommand.isCoverageSummary) {
                bwCoverageDetailsByExon.flush();
                bwCoverageDetailsByExon.close();
            }
            bwSampleExonMatrixSummary.flush();
            bwSampleExonMatrixSummary.close();
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
