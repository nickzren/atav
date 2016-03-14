package function.external.subrvis;

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

    private static final String SUBRVIS_DOMAIN_PATH = "data/subrvis/domain_score_021116.txt";
    private static final String SUBRVIS_EXON_PATH = "data/subrvis/exon_021116.txt";

    private static HashMap<String, ArrayList<SubRvisGene>> geneDomainMap
            = new HashMap<String, ArrayList<SubRvisGene>>();
    private static HashMap<String, ArrayList<SubRvisGene>> geneExonMap
            = new HashMap<String, ArrayList<SubRvisGene>>();

    public static String getTitle() {
        String title = "subRVIS Domain Name,"
                + "subRVIS Domain Score,"
                + "subRVIS Exon Name,"
                + "subRVIS Exon Score";

        return title;
    }

    public static void init() {
        initGeneMap(geneDomainMap, SUBRVIS_DOMAIN_PATH);

        initGeneMap(geneExonMap, SUBRVIS_EXON_PATH);
    }

    private static void initGeneMap(HashMap<String, ArrayList<SubRvisGene>> geneMap, String filePath) {
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

                SubRvisGene subRvisGene = new SubRvisGene(id, chr, regionList, score);

                if (geneMap.containsKey(geneName)) {
                    geneMap.get(geneName).add(subRvisGene);
                } else {
                    ArrayList<SubRvisGene> idArray = new ArrayList<SubRvisGene>();
                    idArray.add(subRvisGene);
                    geneMap.put(geneName, idArray);
                }
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

    public static SubRvisGene getGeneDomain(String geneName, String chr, int pos) {
        ArrayList<SubRvisGene> domainMap = geneDomainMap.get(geneName);

        if (domainMap != null) {
            for (SubRvisGene domain : domainMap) {
                if (domain.isPositionIncluded(chr, pos)) {
                    return domain;
                }
            }
        }

        return null;
    }

    public static SubRvisGene getExonDomain(String geneName, String chr, int pos) {
        ArrayList<SubRvisGene> exonMap = geneExonMap.get(geneName);

        if (exonMap != null) {
            for (SubRvisGene exon : exonMap) {
                if (exon.isPositionIncluded(chr, pos)) {
                    return exon;
                }
            }
        }

        return null;
    }
}
