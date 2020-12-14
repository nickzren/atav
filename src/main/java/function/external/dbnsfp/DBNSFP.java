package function.external.dbnsfp;

import global.Data;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class DBNSFP {

    String transcriptID;
    String siftPred;
    String polyphen2HDIVPred;
    String polyphen2HVARPred;
    String lrtPred;
    String mutationTasterPred;

    public DBNSFP() {
        transcriptID = Data.STRING_NA;
        siftPred = Data.STRING_NA;
        polyphen2HDIVPred = Data.STRING_NA;
        polyphen2HVARPred = Data.STRING_NA;
        lrtPred = Data.STRING_NA;
        mutationTasterPred = Data.STRING_NA;
    }

    public DBNSFP(CSVRecord record) {
        transcriptID = FormatManager.getString(record, "dbNSFP Ensembl_transcriptid");
        siftPred = FormatManager.getString(record, "SIFT_pred");
        polyphen2HDIVPred = FormatManager.getString(record, "Polyphen2_HDIV_pred");
        polyphen2HVARPred = FormatManager.getString(record, "Polyphen2_HVAR_pred");
        lrtPred = FormatManager.getString(record, "LRT_pred");
        mutationTasterPred = FormatManager.getString(record, "MutationTaster_pred");
    }

    public boolean isValid(int stableId) {
        if (transcriptID.equals(Data.STRING_NA)) {
            return false;
        }

        String[] transcripts = transcriptID.split(";");
        String[] siftPreds = siftPred.split(";");
        String[] polyphen2HDIVPreds = polyphen2HDIVPred.split(";");
        String[] polyphen2HVARPreds = polyphen2HVARPred.split(";");
        String[] lrtPreds = lrtPred.split(";");
        String[] mutationTasterPreds = mutationTasterPred.split(";");

        int validCount = 0;
        for (int i = 0; i < transcripts.length; i++) {
            int id = Integer.valueOf(transcripts[i]);

            if (id != stableId) {
                continue;
            }

            // if length match then include value when transcript id matched 
            if (transcripts.length == siftPreds.length) {
                siftPred = siftPreds[i];
            }

            if (transcripts.length == polyphen2HDIVPreds.length) {
                polyphen2HDIVPred = polyphen2HDIVPreds[i];
            }

            if (transcripts.length == polyphen2HVARPreds.length) {
                polyphen2HVARPred = polyphen2HVARPreds[i];
            }

            if (transcripts.length == lrtPreds.length) {
                lrtPred = lrtPreds[i];
            }

            if (transcripts.length == mutationTasterPreds.length) {
                mutationTasterPred = mutationTasterPreds[i];
            }

            if (siftPred.equals(Data.STRING_NA)
                    && polyphen2HDIVPred.equals(Data.STRING_NA)
                    && polyphen2HVARPred.equals(Data.STRING_NA)
                    && lrtPred.equals(Data.STRING_NA)
                    && mutationTasterPred.equals(Data.STRING_NA)) {
                return false;
            }

            if (siftPred.equals("D") || siftPred.equals(Data.STRING_NA)) {
                validCount++;
            }

            if (polyphen2HDIVPred.equals("D") || polyphen2HDIVPred.equals(Data.STRING_NA)) {
                validCount++;
            }

            if (polyphen2HVARPred.equals("D") || polyphen2HVARPred.equals(Data.STRING_NA)) {
                validCount++;
            }

            if (lrtPred.equals("D") || lrtPred.equals(Data.STRING_NA)) {
                validCount++;
            }

            if (mutationTasterPred.equals("A") || mutationTasterPred.equals("D")
                    || mutationTasterPred.equals(Data.STRING_NA)) {
                validCount++;
            }

            // all pass
            if(DBNSFPCommand.isFilterDBNSFPAll && validCount == 5) {
                return true;
            }
            
            // at least one pass
            if(DBNSFPCommand.isFilterDBNSFPOne && validCount >= 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        sj.add(transcriptID);
        sj.add(siftPred);
        sj.add(polyphen2HDIVPred);
        sj.add(polyphen2HVARPred);
        sj.add(lrtPred);
        sj.add(mutationTasterPred);

        return sj.toString();
    }
}
