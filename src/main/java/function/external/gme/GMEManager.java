package function.external.gme;

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
import org.apache.commons.csv.CSVRecord;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class GMEManager {

    private static final String GME_PATH = "data/gme/variome.trim_PanTro2_sampgenes.allsamples.gme_af.tsv.gz";

    private static final HashMap<String, Float> gmeMap = new HashMap<>();

    public static String getHeader() {
        return "GME AF";
    }

    public static String getVersion() {
        return "GME: " + DataManager.getVersion(GME_PATH) + "\n";
    }

    public static void init() {
        if (GMECommand.isInclude) {
            initGmeMap();
        }
    }

    private static void initGmeMap() {
        try {
            File f = new File(Data.ATAV_HOME + GME_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String lineStr = "";
            boolean isHeader = true;
            while ((lineStr = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] tmp = lineStr.split("\t");
                
                if(tmp.length != 5) {
                    continue;
                }
                
                StringJoiner sj = new StringJoiner("-");
                sj.add(tmp[0]);
                sj.add(tmp[1]);
                sj.add(tmp[2]);
                sj.add(tmp[3]);
                
                String variantID = sj.toString(); // chr-pos-ref-alt
                float af = Float.valueOf(tmp[4]);

                gmeMap.put(variantID, af);
            }

            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    public static float getAF(String variantID) {
        Float af = gmeMap.get(variantID);

        return af == null ? Data.FLOAT_NA : af;
    }
    
    public static float getAF(CSVRecord record) {
        return FormatManager.getFloat(record.get(getHeader()));
    }
}
