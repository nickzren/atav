package function.external.synrvis;

import function.external.base.DataManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class SynRvisManager {

    private static final String SynRvis_PATH = "data/synrvis/synRVIS_synGERP_050720.csv.gz";

    private static String header;
    private static final HashMap<String, String> synRvis = new HashMap<>();
    private static StringJoiner NA = new StringJoiner(",");

    public static String getHeader() {
        return header;
    }

    public static String getVersion() {
        return "synRVIS: " + DataManager.getVersion(SynRvis_PATH) + "\n";
    }

    public static void init() {
        if (SynRvisCommand.isInclude) {
            initSynRvisMap();
        }
    }

    public static void initSynRvisMap() {
        try {
            File f = new File(Data.ATAV_HOME + SynRvis_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String line = "";
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                int firstCommaIndex = line.indexOf(",");
                String geneName = line.substring(0, firstCommaIndex);
                String values = line.substring(firstCommaIndex + 1);

                if (isFirstLine) {
                    header = values;

                    for (int i = 0; i < values.split(",").length; i++) {
                        NA.add(Data.STRING_NA);
                    }

                    isFirstLine = false;
                } else {
                    synRvis.put(geneName, values);
                }
            }

            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getLine(String geneName) {
        String line = synRvis.get(geneName);

        return line == null ? NA.toString() : line;
    }
}
