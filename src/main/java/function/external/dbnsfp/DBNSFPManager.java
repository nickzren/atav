package function.external.dbnsfp;

import function.external.base.DataManager;
import function.variant.base.RegionManager;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class DBNSFPManager {

    static final String table = "dbNSFP4_1a.variant_chr";

    private static final HashMap<String, PreparedStatement> preparedStatement4VariantMap = new HashMap<>();

    public static void init() {
        if (DBNSFPCommand.isInclude) {
            for (String chr : RegionManager.ALL_CHR) {
                String sql = "SELECT Ensembl_transcriptid,SIFT_pred,Polyphen2_HDIV_pred,Polyphen2_HVAR_pred,LRT_pred,MutationTaster_pred FROM "
                        + table + chr + " WHERE pos=? AND alt=?";
                preparedStatement4VariantMap.put(chr, DBManager.initPreparedStatement(sql));
            }
        }
    }

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("dbNSFP Ensembl_transcriptid");
        sj.add("SIFT_pred");
        sj.add("Polyphen2_HDIV_pred");
        sj.add("Polyphen2_HVAR_pred");
        sj.add("LRT_pred");
        sj.add("MutationTaster_pred");

        return sj.toString();
    }

    public static String getVersion() {
        return "dbNSFP: " + DataManager.getVersion(table) + "\n";
    }

    public static DBNSFP getDBNSFP(String chr, int pos, String alt, boolean isSNV, HashSet<Integer> allTranscriptSet) {
        DBNSFP dbNSFP = new DBNSFP();

        if (isSNV) {
            try {
                PreparedStatement preparedStatement = preparedStatement4VariantMap.get(chr);
                preparedStatement.setInt(1, pos);
                preparedStatement.setString(2, alt);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    String transcriptID = FormatManager.getString(rs.getString("Ensembl_transcriptid"));
                    String siftPred = FormatManager.getString(rs.getString("SIFT_pred"));
                    String polyphen2HDIVPred = FormatManager.getString(rs.getString("Polyphen2_HDIV_pred"));
                    String polyphen2HVARPred = FormatManager.getString(rs.getString("Polyphen2_HVAR_pred"));
                    String lrtPred = FormatManager.getString(rs.getString("LRT_pred"));
                    String mutationTasterPred = FormatManager.getString(rs.getString("MutationTaster_pred"));

                    String[] transcripts = transcriptID.split(";");
                    String[] siftPreds = siftPred.split(";");
                    String[] polyphen2HDIVPreds = polyphen2HDIVPred.split(";");
                    String[] polyphen2HVARPreds = polyphen2HVARPred.split(";");
                    String[] lrtPreds = lrtPred.split(";");
                    String[] mutationTasterPreds = mutationTasterPred.split(";");

                    StringJoiner transcriptSJ = new StringJoiner(";");
                    StringJoiner siftPredsSJ = new StringJoiner(";");
                    StringJoiner polyphen2HDIVPredSJ = new StringJoiner(";");
                    StringJoiner polyphen2HVARPredSJ = new StringJoiner(";");
                    StringJoiner lrtPredSJ = new StringJoiner(";");
                    StringJoiner mutationTasterPredSJ = new StringJoiner(";");

                    for (int i = 0; i < transcripts.length; i++) {
                        int id = Integer.valueOf(transcripts[i]);

                        if (allTranscriptSet.contains(id)) {
                            transcriptSJ.add(transcripts[i]);

                            // if length match then include value when transcript id matched 
                            if (transcripts.length == siftPreds.length) {
                                siftPredsSJ.add(siftPreds[i]);
                            }

                            if (transcripts.length == polyphen2HDIVPreds.length) {
                                polyphen2HDIVPredSJ.add(polyphen2HDIVPreds[i]);
                            }

                            if (transcripts.length == polyphen2HVARPreds.length) {
                                polyphen2HVARPredSJ.add(polyphen2HVARPreds[i]);
                            }

                            if (transcripts.length == lrtPreds.length) {
                                lrtPredSJ.add(lrtPreds[i]);
                            }

                            if (transcripts.length == mutationTasterPreds.length) {
                                mutationTasterPredSJ.add(mutationTasterPreds[i]);
                            }
                        }
                    }

                    // if length not match then use original value
                    if (transcripts.length != siftPreds.length) {
                        siftPredsSJ.add(siftPred);
                    }

                    if (transcripts.length != polyphen2HDIVPreds.length) {
                        polyphen2HDIVPredSJ.add(polyphen2HDIVPred);
                    }

                    if (transcripts.length != polyphen2HVARPreds.length) {
                        polyphen2HVARPredSJ.add(polyphen2HVARPred);
                    }

                    if (transcripts.length != lrtPreds.length) {
                        lrtPredSJ.add(lrtPred);
                    }

                    if (transcripts.length != mutationTasterPreds.length) {
                        mutationTasterPredSJ.add(mutationTasterPred);
                    }

                    dbNSFP.transcriptID = getNonEmptyString(transcriptSJ.toString());
                    dbNSFP.siftPred = getNonEmptyString(siftPredsSJ.toString());
                    dbNSFP.polyphen2HDIVPred = getNonEmptyString(polyphen2HDIVPredSJ.toString());
                    dbNSFP.polyphen2HVARPred = getNonEmptyString(polyphen2HVARPredSJ.toString());
                    dbNSFP.lrtPred = getNonEmptyString(lrtPredSJ.toString());
                    dbNSFP.mutationTasterPred = getNonEmptyString(mutationTasterPredSJ.toString());
                }

                rs.close();
            } catch (SQLException ex) {
                ErrorManager.send(ex);
            }
        }

        return dbNSFP;
    }

    public static boolean isValid(DBNSFP dbNSFP, int id) {
        if (!DBNSFPCommand.isInclude) {
            return true;
        }

        return dbNSFP.isValid(id);
    }

    private static String getNonEmptyString(String value) {
        return value.isEmpty() ? Data.STRING_NA : value.replaceAll("\\.", Data.STRING_NA);
    }
}
