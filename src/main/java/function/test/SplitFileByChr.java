package function.test;

import function.variant.base.RegionManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import utils.CommonCommand;

/**
 *
 * @author nick
 */
public class SplitFileByChr {

    private static HashMap<String, BufferedWriter> bwMap = new HashMap();

    public static void run(String input) throws Exception {
        File file = new File(input);
        String fileName = file.getName();

        for (String chr : RegionManager.ALL_CHR) {
            String outputFileName = CommonCommand.realOutputPath + File.separator + chr + "_" + fileName;
            bwMap.put(chr, new BufferedWriter(new FileWriter(new File(outputFileName))));
        }

        BufferedReader br = new BufferedReader(new FileReader(file));
        String lineStr = "";
        while ((lineStr = br.readLine()) != null) {
            String chr = lineStr.split("\t")[0];

            if (RegionManager.isChrValid(chr)) {
                bwMap.get(chr).write(lineStr);
                bwMap.get(chr).newLine();
            }
        }

        close();
    }

    private static void close() throws IOException {
        for (String chr : RegionManager.ALL_CHR) {
            bwMap.get(chr).flush();
            bwMap.get(chr).close();
        }
    }
}
