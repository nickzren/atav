package atav.annotools;

import atav.analysis.base.AnalysisBase;
import atav.analysis.base.AnnotatedVariant;
import atav.manager.data.EvsManager;
import atav.manager.utils.CommandValue;
import atav.manager.utils.ErrorManager;
import atav.manager.utils.LogManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 *
 * @author nick
 */
public class JonEvsTool extends AnalysisBase {

    BufferedWriter bwDetails = null;

    @Override
    public void initOutput() {
        try {
            File f = new File(CommandValue.jonEvsInput);
            String pipelineOutput = CommandValue.outputPath + f.getName();
            bwDetails = new BufferedWriter(new FileWriter(pipelineOutput));
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
            bwDetails.flush();
            bwDetails.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void run() {
        try {
            initOutput();

            beforeProcessDatabaseData();

            processDatabaseData();

            afterProcessDatabaseData();

            closeOutput();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void processDatabaseData() throws Exception {
        String lineStr = "";
        int lineNum = 0;

        try {
            File f = new File(CommandValue.jonEvsInput);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineNum == 1) {
                    bwDetails.write(lineStr
                            + "\t"
                            + EvsManager.getTitle().replaceAll(",", "\t")
                            + "\n");
                    continue;
                }

                if (lineStr.isEmpty()) {
                    continue;
                }

                String[] temp = lineStr.split("\t");
                String chr = temp[0];
                int pos = Integer.valueOf(temp[1]);
                String rs = temp[2];
                String ref = temp[3];
                String alt = temp[4];
                boolean isIndel = ref.length() != alt.length();

                AnnotatedVariant var = new AnnotatedVariant(0, isIndel, alt, ref, rs, pos, chr);

                bwDetails.write(lineStr + "\t"
                        + var.getEvsCoverageStr().replaceAll(",", "\t") + "\t"
                        + var.getEvsMafStr().replaceAll(",", "\t") + "\t"
                        + var.getEvsFilterStatus().replaceAll(",", "\t") + "\n");
            }
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in pipeline input file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "It is running a evs pipeline function...";
    }
}
