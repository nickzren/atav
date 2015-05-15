package atav.manager.data;

import atav.global.Data;
import atav.manager.utils.CommandValue;
import atav.manager.utils.FormatManager;
import java.io.*;
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class IntolerantScoreManager {

    private static HashMap<String, IntolerantScore> intolerantScoreMap = new HashMap<String, IntolerantScore>();

    public static String getTitle() {
        return "RVIS ALL_0.1%MAF Percentile,"
                + "RVIS OEratio Percentile,"
                + "RVIS EdgeCase,";
    }

    public static void init() throws Exception {
        String intolerantScoreFile = Data.INTOLERANT_SCORE_PATH;

        if (CommandValue.isDebug) {
            intolerantScoreFile = Data.RECOURCE_PATH + intolerantScoreFile;
        }

        File f = new File(intolerantScoreFile);
        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        while ((line = br.readLine()) != null) {
            if (line.contains("GENE")) {
                continue;
            }

            if (!line.isEmpty()) {
                String[] values = line.split("\t");

                IntolerantScore score = new IntolerantScore(values);

                intolerantScoreMap.put(score.getGeneName(), score);
            }
        }
    }

    public static String getValues(String geneName) {
        IntolerantScore score = intolerantScoreMap.get(geneName);

        String values = "NA,NA,NA";

        if (score != null) {
            values = FormatManager.getDouble(score.getall01Percentile()) + ","
                    + FormatManager.getDouble(score.getOEratioPercentile()) + ","
                    + score.getEdgeCase();
        }

        return values;
    }
}
