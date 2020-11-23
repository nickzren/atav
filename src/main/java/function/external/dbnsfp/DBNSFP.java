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
