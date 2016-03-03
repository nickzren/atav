package function.external.subrvis;

import global.Data;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class SubRvisManager {

    private static HashMap<String, SubRvisGene> geneDomainMap = new HashMap<String, SubRvisGene>();
    private static HashMap<String, SubRvisGene> geneExonMap = new HashMap<String, SubRvisGene>();

    public static String getTitle() {
        String title = "subRVIS Domain Name,"
                + "subRVIS Domain Score,"
                + "subRVIS Exon Name,"
                + "subRVIS Exon Score";

        return title;
    }

    public static void init() {
        initGeneMap(geneDomainMap, Data.SUBRVIS_DOMAIN);

        initGeneMap(geneExonMap, Data.SUBRVIS_EXON);
    }

    private static void initGeneMap(HashMap<String, SubRvisGene> geneMap, String filePath) {
        try {
            File f = new File(filePath);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                String[] tmp = lineStr.split(" ");

                String geneName = tmp[0];
                String id = tmp[1];
                String[] regionStr = tmp[2].split(":");
                String chr = regionStr[0];
                ArrayList<Region> regionList = getRegionList(regionStr[1]);
                float score = Float.valueOf(tmp[3]);

                SubRvisGene subRvisGene = new SubRvisGene(geneName, id, chr, regionList, score);

                geneMap.put(geneName, subRvisGene);
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static ArrayList<Region> getRegionList(String regionStr) {
        String[] strArray = regionStr.split(",");

        ArrayList<Region> regionList = new ArrayList<Region>();

        for (int i = 0; i < strArray.length; i++) {
            String[] tmp = strArray[i].split("-");

            int start = Integer.valueOf(tmp[0]);
            int end = Integer.valueOf(tmp[1]);
            Region region = new Region(start, end);

            regionList.add(region);
        }

        return regionList;
    }

    public static SubRvisGene getGeneDomain(String geneName) {
        return geneDomainMap.get(geneName);
    }

    public static SubRvisGene getExonDomain(String geneName) {
        return geneExonMap.get(geneName);
    }
}
