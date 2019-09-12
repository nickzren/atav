package function.external.mgi;

import function.external.base.DataManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class MgiManager {

    private static final String MGI_PATH = "data/mgi/mouse_essential_gene_091019.csv";

    public static String title;
    private static final HashMap<String, String> mgiMap = new HashMap<>();
    private static StringJoiner NA = new StringJoiner(",");

    public static String getTitle() {
        return title;
    }

    public static String getVersion() {
        return "MGI: " + DataManager.getVersion(MGI_PATH) + "\n";
    }

    public static void init() {
        if (MgiCommand.isIncludeMgi) {
            initMgiMap();
        }
    }

    private static void initMgiMap() {
        try {
            File f = new File(Data.ATAV_HOME + MGI_PATH);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                int firstCommaIndex = lineStr.indexOf(",");
                String geneName = lineStr.substring(0, firstCommaIndex);
                String values = lineStr.substring(firstCommaIndex + 1);

                if (geneName.equals("Gene")) {
                    title = values;

                    for (int i = 0; i < values.split(",").length; i++) {
                        NA.add(Data.STRING_NA);
                    }
                } else {
                    mgiMap.put(geneName, values);
                }
            }

            br.close();
            fr.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getLine(String geneName) {
        String line = mgiMap.get(geneName);

        return line == null ? NA.toString() : line;
    }
}
