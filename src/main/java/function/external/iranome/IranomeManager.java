package function.external.iranome;

import function.external.base.DataManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVRecord;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class IranomeManager {
    private static final String IRANOME_PATH = "data/iranome/Iranome_Variants_Frequency_b37.csv.gz";

    private static final HashMap<String, Float> iranomeMap = new HashMap<>();

    public static String getHeader() {
        return "Iranome AF";
    }

    public static String getVersion() {
        return "Iranome: " + DataManager.getVersion(IRANOME_PATH) + "\n";
    }

    public static void init() {
        if (IranomeCommand.isInclude) {
            initIranomeMap();
        }
    }

    private static void initIranomeMap() {
        try {
            File f = new File(Data.ATAV_HOME + IRANOME_PATH);
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

                String[] tmp = lineStr.split(",");
               
                String variantID = tmp[0]; // chr-pos-ref-alt
                float af = Float.valueOf(tmp[1]);

                iranomeMap.put(variantID, af);
            }

            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    public static float getAF(String variantID) {
        Float af = iranomeMap.get(variantID);

        return af == null ? Data.FLOAT_NA : af;
    }
    
    public static float getAF(CSVRecord record) {
        return FormatManager.getFloat(record.get(getHeader()));
    }
}
