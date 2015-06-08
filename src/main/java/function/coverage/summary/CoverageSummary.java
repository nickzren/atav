package function.coverage.summary;

import function.coverage.base.SampleStatistics;
import function.coverage.base.CoveredRegion;
import function.coverage.base.Exon;
import function.coverage.base.Gene;
import function.coverage.base.InputList;
import function.coverage.base.Transcript;
import global.Data;
import function.genotype.base.SampleManager;
import utils.CommandValue;
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

    boolean isSystemGeneIndexFile = false;
    public BufferedWriter bwSampleSummary = null;
    public BufferedWriter bwSampleRegionSummary = null;
    public BufferedWriter bwSampleMatrixSummary = null;
    public BufferedWriter bwSampleExonMatrixSummary = null;
    public BufferedWriter bwCoverageDetailsByExon = null;
    public BufferedWriter bwCoverageSummaryByExon = null;
    public BufferedWriter bwCoverageSummaryByGene = null;

    public final String sampleSummaryFilePath = CommandValue.outputPath + "sample.summary.csv";
    public final String coverageDetailsFilePath = CommandValue.outputPath + "coverage.details.csv";
    public final String coverageDetailsByExonFilePath = CommandValue.outputPath + "coverage.details.by.exon.csv";
    public final String coverageMatrixFilePath = CommandValue.outputPath + "coverage.details.matrix.csv";
    public final String coverageExonMatrixFilePath = CommandValue.outputPath + "coverage.details.matrix.by.exon.csv";

    public CoverageSummary() {
        super();
        
        if (CommandValue.minCoverage == Data.NO_FILTER) {
            ErrorManager.print("--min-coverage option has to be used in this function.");
        }

        try {
            isSystemGeneIndexFile = CommandValue.coveredRegionFile.contains("/nfs/goldstein/software/atav_home/data");
            BufferedReader br = new BufferedReader(new FileReader(CommandValue.coveredRegionFile));
            String str;
            int LineCount = 0;
            while ((str = br.readLine()) != null && str.length() > 0) {
                try {
                    if (!addRegion(str)) {
                        Transcript transcript = new Transcript(str);
                        if (transcript.isValid()) {
                            add(transcript);
                        } else {
                            Gene gene = new Gene(str);
                            if (gene.isValid()) {
                                add(gene);
                            }
                        }
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

    public void DoExonSummary(SampleStatistics ss, int record, HashMap<Integer, Integer> result, Exon e) throws Exception {
        ss.print(record, result, e, bwCoverageDetailsByExon);
    }

    public void DoGeneSummary(SampleStatistics ss, int record) throws Exception {
        //do nothing for coverage summary
    }

    public void run() throws Exception {
        initOutput();
        String strSamples = SampleManager.getAllSampleId();
        SampleStatistics ss = new SampleStatistics(strSamples, size());
        ss.printMatrixHeader(bwSampleMatrixSummary, false);

        if (CommandValue.isByExon) {
            ss.printMatrixHeader(bwSampleExonMatrixSummary, true);
        }

        for (Iterator it = this.iterator(); it.hasNext();) {
            int record = ss.getNextRecord();

            Object obj = it.next();
            if (!CommandValue.isTerse) {
                System.out.print("Processing " + (record + 1) + " of " + size() + ":        " + obj.toString() + "                              \r");
            }

            boolean NeedToUpdateDatabase = false;
            String JobType = obj.getClass().getSimpleName();
            //the following should be simplified by implementing a same interface. Q.
            if (JobType.contains("CoveredRegion")) {
                CoveredRegion region = (CoveredRegion) obj;
                ss.setRecordName(record, region.toString(), region.getChrStr());
                ss.setLength(record, region.getLength());
                int[] mincovs = {CommandValue.minCoverage};
                HashMap<Integer, Integer> result = region.getCoverage(mincovs).get(0);
                ss.accumulateCoverage(record, result);
            } else if (JobType.equals("Gene")) {
                String trans_stable_id = "";
                Gene gene = (Gene) obj;
                ss.setRecordName(record, gene.toString(), gene.getChr());
                if (gene.getType().equalsIgnoreCase("Slave")) {
                    NeedToUpdateDatabase = true;
                    gene.populateSlaveList();
                    ss.setRecordName(record, gene.getName(), gene.getChr());
                    ss.setLength(record, gene.getLength());

                    trans_stable_id = "CONSENOUS_TRANSCRIPT";
                    if (!CommandValue.isByExon && isSystemGeneIndexFile) { //if by exon, then we can't use gene_coverage_summary as we don't have exon info
                        HashMap<Integer, Double> cv = gene.getCoverageFromTable();
                        if (cv.size() == SampleManager.getListSize()) {
                            NeedToUpdateDatabase = false;
                            ss.setGeneCoverage(record, cv);
                        }
                    }

                } else {
                    if (!CommandValue.isCcdsOnly || gene.isCCDS()) {
                        gene.populateExonList();
                        if (CommandValue.isExcludeUTR) {
                            gene.filterByUTR();
                        }
                    } else { //deal with --ccsds-only but canonical transcript is not ccds
                        int transcriptid = gene.populateExonListFromTranscriptID();
                        if (CommandValue.isExcludeUTR && transcriptid > 0) {
                            gene.filterByUTRFromTranscriptID(transcriptid);
                        }
                    }
                    ss.setRecordName(record, gene.getName(), gene.getChrFromExon());
                    ss.setLength(record, gene.getLength());
                }

                if (NeedToUpdateDatabase || !gene.getType().equalsIgnoreCase("Slave")) {
                    for (Iterator r = gene.getExonList().iterator(); r.hasNext();) {
                        Exon exon = (Exon) r.next();
                        trans_stable_id = exon.getTransStableId();
                        HashMap<Integer, Integer> result = exon.getCoverage(CommandValue.minCoverage);
                        ss.accumulateCoverage(record, result);
                        ss.printMatrixRowbyExon(record, result, exon, bwSampleExonMatrixSummary);
                        DoExonSummary(ss, record, result, exon);
                    }
                }

                LogManager.writeLog("Gene ENSTID: " + gene.getName() + "(" + gene.getChr() + ")\t" + trans_stable_id + "\t(" + (record + 1) + " of " + size() + ")");

            } else if (JobType.equals("Transcript")) {
                Transcript transcript = (Transcript) obj;
                ss.setRecordName(record, transcript.toString(), "");
                if (!CommandValue.isCcdsOnly || transcript.isCCDS()) {
                    transcript.populateExonList();
                    if (CommandValue.isExcludeUTR) {
                        transcript.filterByUTR();
                    }
                    ss.setLength(record, transcript.getLength());
                    for (Iterator r = transcript.getExonList().iterator(); r.hasNext();) {
                        Exon exon = (Exon) r.next();
                        HashMap<Integer, Integer> result = exon.getCoverage(CommandValue.minCoverage);
                        ss.accumulateCoverage(record, result);
                        if (CommandValue.isCoverageSummary) {
                            ss.print(record, result, exon, bwCoverageDetailsByExon);
                        }
                    }
                }
            } else {
                ErrorManager.print("Coverage: undefined object type found: " + JobType);
            }
            ss.print(record, bwSampleRegionSummary);
            ss.printMatrixRow(record, bwSampleMatrixSummary);
            DoGeneSummary(ss, record);
            if (NeedToUpdateDatabase && isSystemGeneIndexFile) {
                ss.updateMatrixRowInDataBase(record);
            }
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

        if (CommandValue.isByExon) {
            if (CommandValue.isCoverageSummary) {
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

        if (CommandValue.isByExon) {
            if (CommandValue.isCoverageSummary) {
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
