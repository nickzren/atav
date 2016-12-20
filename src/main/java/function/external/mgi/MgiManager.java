package function.external.mgi;

import function.external.base.DataManager;
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
public class MgiManager {

    private static final String MGI_PATH = "data/mgi/mouse_essential_gene_122016.csv";

    public static String title;
    private static final HashMap<String, String> mgiMap = new HashMap<>();
    public static String NA = "";

    public static String getTitle() {
        if (MgiCommand.isIncludeMgi) {
            return title + ",";
        } else {
            return "";
        }
    }

    public static String getVersion() {
        if (MgiCommand.isIncludeMgi) {
            return "MGI: " + DataManager.getVersion(MGI_PATH) + "\n";
        } else {
            return "";
        }
    }
    
    public static void init() {
        if (MgiCommand.isIncludeMgi) {
            initMgiMap();
        }
    }

    private static void initMgiMap() {
        try {
            File f = new File(MGI_PATH);
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
                    mgiMap.put(geneName, values + ",");
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
        String line = mgiMap.get(geneName);

        return line == null ? NA : line;
    }
}
