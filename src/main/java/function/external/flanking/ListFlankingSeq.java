package function.external.flanking;

import function.base.AnalysisBase;
import function.variant.base.RegionManager;
import utils.CommandValue;
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

    final String updateFlankingSeqFilePath = CommandValue.outputPath + "updateflankingseq.csv";
    final String baseFlankingSeqFilePath = CommandValue.outputPath;

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
        File f = new File(baseFlankingSeqFilePath + "baseflankingseq.csv");

        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        bwUpdateFlankingSeq.write(br.readLine() + "\n"); // write title

        while ((line = br.readLine()) != null) {
            String[] lineStrArray = line.split(",");

            String seqStr = lineStrArray[3];

            StringBuilder leftSeqSb = new StringBuilder(seqStr.substring(0, seqStr.indexOf("[")));
            String centerStr = seqStr.substring(seqStr.indexOf("["), seqStr.indexOf("]") + 1);
            StringBuilder rightSeqSb = new StringBuilder(seqStr.substring(seqStr.indexOf("]") + 1));

            updateFlankingSeq(lineStrArray[0], leftSeqSb, rightSeqSb);

            bwUpdateFlankingSeq.write(
                    lineStrArray[0] + "," // variantId
                    + lineStrArray[1] + "," // allele
                    + lineStrArray[2] + "," // refAllele
                    + leftSeqSb + centerStr + rightSeqSb + "\n");
        }
    }

    private void updateFlankingSeq(String variantId, StringBuilder leftSeqSb,
            StringBuilder rightSeqSb) throws Exception {
        String[] variantIdStrArray = variantId.split("_");

        String chr = variantIdStrArray[0];

        String indelType = "";

        int startPos = Integer.valueOf(variantIdStrArray[1]);
        int endPos = startPos;

        if (variantIdStrArray.length > 3) {
            indelType = variantIdStrArray[3];

            endPos = Integer.valueOf(variantIdStrArray[2]);
        }

        String sql = generateSql(chr, startPos, endPos, indelType);

        ResultSet rs = DBManager.executeQuery(sql);

        while (rs.next()) {
            int varPos = rs.getInt("seq_region_pos");

            updateFlankingSeqByOnePos(startPos, endPos, indelType,
                    varPos, leftSeqSb, rightSeqSb);
        }
    }

    private String generateSql(String chr, int startPos, int endPos, String indelType) {
        int regionId = RegionManager.getIdByChr(chr);

        int seqStart = startPos - CommandValue.width;
        int seqEnd = startPos + CommandValue.width;

        if (!indelType.isEmpty()) {
            seqStart += 1;

            if (indelType.equals("DEL")) {
                seqEnd = endPos + 1 + CommandValue.width;
            }
        }

        String sql = "SELECT DISTINCT seq_region_pos"
                + " FROM snv"
                + " WHERE seq_region_id = " + regionId
                + " AND seq_region_pos BETWEEN " + seqStart
                + " AND " + seqEnd;

        return sql;
    }

    private void updateFlankingSeqByOnePos(int startPos, int endPos, String indelType,
            int varPos, StringBuilder leftSeqSb, StringBuilder rightSeqSb) {
        if (indelType.isEmpty()) { // snv
            if (varPos < startPos) {
                int index = varPos - (startPos - CommandValue.width);

                leftSeqSb.setCharAt(index, 'N');
            } else if (varPos > startPos) {
                int index = varPos - startPos - 1;

                rightSeqSb.setCharAt(index, 'N');
            }
        } else { // indel
            if (varPos <= startPos) {
                int index = varPos - (startPos - CommandValue.width + 1);

                leftSeqSb.setCharAt(index, 'N');
            } else {
                if (indelType.equals("INS")) {
                    int index = varPos - startPos - 1;

                    rightSeqSb.setCharAt(index, 'N');
                } else if (varPos > endPos + 1) {
                    int index = varPos - (endPos + 2);

                    rightSeqSb.setCharAt(index, 'N');
                }
            }
        }
    }

    @Override
    public String toString() {
        return "It is running a list variant flanking sequence function...";
    }
}
