package function.external.mgi;

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
public class MgiManager {

    private static final String MGI_PATH = "data/mgi/mouse_essential_gene_040821.csv.gz";

    public static String header;
    private static final HashMap<String, String> mgiMap = new HashMap<>();
    private static StringJoiner NA = new StringJoiner(",");

    public static String getHeader() {
        return header;
    }

    public static String getVersion() {
        return "MGI: " + DataManager.getVersion(MGI_PATH) + "\n";
    }

    public static void init() {
        if (MgiCommand.isInclude) {
            initMgiMap();
        }
    }

    private static void initMgiMap() {
        try {
            File f = new File(Data.ATAV_HOME + MGI_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                int firstCommaIndex = lineStr.indexOf(",");
                String geneName = lineStr.substring(0, firstCommaIndex);
                String values = lineStr.substring(firstCommaIndex + 1);

                if (geneName.equals("Gene")) {
                    header = values;

                    for (int i = 0; i < values.split(",").length; i++) {
                        NA.add(Data.STRING_NA);
                    }
                } else {
                    mgiMap.put(geneName, values);
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
        String line = mgiMap.get(geneName);

        return line == null ? NA.toString() : line;
    }
}
