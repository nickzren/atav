package function.external.ccr;

import function.external.base.DataManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CCRManager {

    private static final String PATH = "data/ccr/2019-4-23_ccrScores.txt.gz";

    private static HashMap<String, ArrayList<CCRGene>> geneMap = new HashMap<>();

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("CCR Region Name");
        sj.add("CCR Percentile");

        return sj.toString();
    }

    public static String getVersion() {
        return "CCR: " + DataManager.getVersion(PATH) + "\n";
    }

    public static void init() {
        if (CCRCommand.isInclude) {
            initGeneMap(geneMap, Data.ATAV_HOME + PATH);
        }
    }

    private static void initGeneMap(HashMap<String, ArrayList<CCRGene>> geneMap, String filePath) {
         String lineStr = "";
        try {
            File f = new File(filePath);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);
            
           
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                String[] tmp = lineStr.split("\t");

                String geneName = tmp[0];
                String id = tmp[1];
                String[] regionStr = tmp[2].split(":");
                String chr = regionStr[0];
                ArrayList<Region> regionList = getRegionList(regionStr[1]);
                float percentiles = FormatManager.getFloat(tmp[3]);

                CCRGene ccrGene = new CCRGene(id, chr, regionList, percentiles);

                if (geneMap.containsKey(geneName)) {
                    geneMap.get(geneName).add(ccrGene);
                } else {
                    ArrayList<CCRGene> idArray = new ArrayList<>();
                    idArray.add(ccrGene);
                    geneMap.put(geneName, idArray);
                }
            }

            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            System.out.println(lineStr);
            ErrorManager.send(e);
        }
    }

    private static ArrayList<Region> getRegionList(String regionStr) {
        String[] strArray = regionStr.split(",");

        ArrayList<Region> regionList = new ArrayList<>();

        for (String str : strArray) {
            String[] tmp = str.split("-");
            int start = Integer.valueOf(tmp[0]);
            int end = Integer.valueOf(tmp[1]);
            Region region = new Region(start, end);
            regionList.add(region);
        }

        return regionList;
    }

    public static CCRGene getGene(String geneName, String chr, int pos) {
        ArrayList<CCRGene> geneList = geneMap.get(geneName);

        if (geneList != null) {
            for (CCRGene gene : geneList) {
                if (gene.isPositionIncluded(chr, pos)) {
                    return gene;
                }
            }
        }

        return null;
    }
}
