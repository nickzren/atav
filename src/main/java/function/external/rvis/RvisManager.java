package function.external.rvis;

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
public class RvisManager {

    private static final String RVIS_PATH = "data/rvis/gene_score_140318.csv.gz";

    private static String header;
    private static final HashMap<String, String> rvisMap = new HashMap<>();
    private static StringJoiner NA = new StringJoiner(",");

    public static String getHeader() {
        return header;
    }

    public static String getVersion() {
        return "RVIS: " + DataManager.getVersion(RVIS_PATH) + "\n";
    }

    public static void init() {
        if (RvisCommand.isInclude) {
            initRvisMap();
        }
    }

    private static void initRvisMap() {
        try {
            File f = new File(Data.ATAV_HOME + RVIS_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);
            
            String lineStr = "";
            boolean isFirstLine = true;
            while ((lineStr = br.readLine()) != null) {
                int firstCommaIndex = lineStr.indexOf(",");
                String geneName = lineStr.substring(0, firstCommaIndex);
                String values = lineStr.substring(firstCommaIndex + 1);

                if (isFirstLine) {
                    header = values;

                    for (int i = 0; i < values.split(",").length; i++) {
                        NA.add(Data.STRING_NA);
                    }
                    
                    isFirstLine = false;
                } else {
                    rvisMap.put(geneName, values);
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
        String line = rvisMap.get(geneName);

        return line == null ? NA.toString() : line;
    }
}
