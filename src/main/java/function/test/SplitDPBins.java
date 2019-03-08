package function.test;

import function.variant.base.RegionManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import utils.CommonCommand;

/**
 *
 * @author nick
 */
public class SplitDPBins {

    public static HashMap<String, BufferedWriter> chrWritterMap = new HashMap<>();

    public static void run() throws Exception {
        init();

        Set<String> chrSet = new HashSet<>(Arrays.asList(RegionManager.ALL_CHR));

        File f = new File(TestCommand.dpBinFilePath);
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String lineStr = "";

        while ((lineStr = br.readLine()) != null) {
            String[] lineStrArray = lineStr.split("\t");

            if (lineStrArray.length != 3) {
                continue;
            }

            String line = TestCommand.sampleID + "\t" + lineStrArray[1] + "\t" + lineStrArray[2];

            String chr = "";
            if (chrSet.contains(lineStrArray[0])) {
                chr = lineStrArray[0];
            } else {
                continue;
            }

            chrWritterMap.get(chr).write(line);
            chrWritterMap.get(chr).newLine();
        }

        close();
    }

    private static void init() throws IOException {
        for (String chr : RegionManager.ALL_CHR) {
            String path = CommonCommand.outputPath + "chr" + chr;
            File file = new File(path);
            chrWritterMap.put(chr, new BufferedWriter(new FileWriter(file)));
        }
    }

    private static void close() throws IOException {
        for (String chr : RegionManager.ALL_CHR) {
            chrWritterMap.get(chr).flush();
            chrWritterMap.get(chr).close();
        }
    }
}
