package function.external.rvis;

import function.external.base.DataManager;
import global.Data;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class RvisManager {

    private static final String RVIS_PATH = "data/rvis/gene_score_140318.csv";

    private static String title;
    private static final HashMap<String, String> rvisMap = new HashMap<>();
    private static String NA = "";

    public static String getTitle() {
        if (RvisCommand.isIncludeRvis) {
            return title + ",";
        } else {
            return "";
        }
    }

    public static String getVersion() {
        if (RvisCommand.isIncludeRvis) {
            return "RVIS: " + DataManager.getVersion(RVIS_PATH) + "\n";
        } else {
            return "";
        }
    }

    public static void init() {
        if (RvisCommand.isIncludeRvis) {
            initRvisMap();
        }
    }

    private static void initRvisMap() {
        try {
            File f = new File(Data.ATAV_HOME + RVIS_PATH);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                int firstCommaIndex = lineStr.indexOf(",");
                String geneName = lineStr.substring(0, firstCommaIndex);
                String values = lineStr.substring(firstCommaIndex + 1);

                if (geneName.equals("Gene")) {
                    title = values;

                    for (int i = 0; i < values.split(",").length; i++) {
                        NA += "NA,";
                    }
                } else {
                    rvisMap.put(geneName, values + ",");
                }
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getLine(String geneName) {
        String line = rvisMap.get(geneName);

        return line == null ? NA : line;
    }
}
