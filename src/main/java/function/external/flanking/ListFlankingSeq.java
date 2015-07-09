package function.external.flanking;

import function.AnalysisBase;
import function.variant.base.RegionManager;
import utils.CommonCommand;
import utils.DBManager;
import utils.ErrorManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class ListFlankingSeq extends AnalysisBase {

    BufferedWriter bwUpdateFlankingSeq = null;

    final String updateFlankingSeqFilePath = CommonCommand.outputPath + "flanking_seq_annodb.csv";
    final String baseFlankingSeqFilePath = CommonCommand.outputPath;

    @Override
    public void initOutput() {
        try {
            bwUpdateFlankingSeq = new BufferedWriter(new FileWriter(updateFlankingSeqFilePath));
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {
        try {
            bwUpdateFlankingSeq.flush();
            bwUpdateFlankingSeq.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        ThirdPartyToolManager.callFlankingSeq(baseFlankingSeqFilePath);
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() throws Exception {
        File f = new File(baseFlankingSeqFilePath + "flanking_seq_base.csv");

        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        bwUpdateFlankingSeq.write(br.readLine() + "\n"); // write title

        while ((line = br.readLine()) != null) {
            String[] lineStrArray = line.split(",");

            StringBuilder leftSeqSb = new StringBuilder(lineStrArray[1]);
            StringBuilder rightSeqSb = new StringBuilder(lineStrArray[2]);

            updateFlankingSeq(lineStrArray[0], leftSeqSb, rightSeqSb);

            bwUpdateFlankingSeq.write(
                    lineStrArray[0] + "," // variantId
                    + leftSeqSb + ","
                    + rightSeqSb + "\n");
        }
    }

    private void updateFlankingSeq(String variantId, StringBuilder leftSeqSb,
            StringBuilder rightSeqSb) throws Exception {
        String[] variantIdStrArray = variantId.split("-");

        String chr = variantIdStrArray[0];

        int pos = Integer.valueOf(variantIdStrArray[1]);

        String sql = generateSql(chr, pos);

        ResultSet rs = DBManager.executeQuery(sql);

        while (rs.next()) {
            int varPos = rs.getInt("seq_region_pos");

            updateFlankingSeqByOnePos(pos, varPos, leftSeqSb, rightSeqSb);
        }
    }

    private String generateSql(String chr, int pos) {
        int regionId = RegionManager.getIdByChr(chr);

        int seqStart = pos - FlankingCommand.width;
        int seqEnd = pos + FlankingCommand.width;

        String sql = "SELECT DISTINCT seq_region_pos"
                + " FROM snv"
                + " WHERE seq_region_id = " + regionId
                + " AND seq_region_pos BETWEEN " + seqStart
                + " AND " + seqEnd;

        return sql;
    }

    private void updateFlankingSeqByOnePos(int pos,
            int varPos, StringBuilder leftSeqSb, StringBuilder rightSeqSb) {
        if (varPos < pos) {
            int index = varPos - (pos - FlankingCommand.width);

            leftSeqSb.setCharAt(index, 'N');
        } else if (varPos > pos) {
            int index = varPos - pos - 1;

            rightSeqSb.setCharAt(index, 'N');
        }
    }

    @Override
    public String toString() {
        return "It is running a list variant flanking sequence function...";
    }
}
