package function.external.bis;

import function.external.base.DataManager;
import global.Data;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class BisManager {

    private static final String DOMAIN_PATH = "data/bis/DomainBISPercentile_072617.txt";
    private static final String EXON_PATH = "data/bis/ExonBISPercentile_072617.txt";

    private static HashMap<String, ArrayList<BisGene>> geneDomainMap = new HashMap<>();
    private static HashMap<String, ArrayList<BisGene>> geneExonMap = new HashMap<>();

    public static String getTitle() {
        if (BisCommand.isIncludeBis) {
            return "BIS Domain Name,"
                    + "BIS Domain Score Percentile (0.005),"
                    + "BIS Domain Score Percentile (0.001),"
                    + "BIS Domain Score Percentile (0.0005),"
                    + "BIS Domain Score Percentile (0.0001),"
                    + "BIS Exon Name,"
                    + "BIS Exon Score Percentile (0.005),"
                    + "BIS Exon Score Percentile (0.001),"
                    + "BIS Exon Score Percentile (0.0005),"
                    + "BIS Exon Score Percentile (0.0001),";
        } else {
            return "";
        }
    }

    public static String getVersion() {
        if (BisCommand.isIncludeBis) {
            return "BIS: " + DataManager.getVersion(EXON_PATH) + "\n";
        } else {
            return "";
        }
    }

    public static void init() {
        if (BisCommand.isIncludeBis) {
            initGeneMap(geneDomainMap, Data.ATAV_HOME + DOMAIN_PATH);

            initGeneMap(geneExonMap, Data.ATAV_HOME + EXON_PATH);
        }
    }

    private static void initGeneMap(HashMap<String, ArrayList<BisGene>> geneMap, String filePath) {
        try {
            File f = new File(filePath);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                String[] tmp = lineStr.split(" ");

                String geneName = tmp[0];
                String id = tmp[1];
                String[] regionStr = tmp[2].split(":");
                String chr = regionStr[0];
                ArrayList<Region> regionList = getRegionList(regionStr[1]);
                float score0005 = FormatManager.getFloat(tmp[3]);
                float score0001 = FormatManager.getFloat(tmp[4]);
                float score00005 = FormatManager.getFloat(tmp[5]);
                float score00001 = FormatManager.getFloat(tmp[6]);

                BisGene bisGene = new BisGene(id, chr, regionList, 
                        score0005, score0001, score00005, score00001);

                if (geneMap.containsKey(geneName)) {
                    geneMap.get(geneName).add(bisGene);
                } else {
                    ArrayList<BisGene> idArray = new ArrayList<>();
                    idArray.add(bisGene);
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

    public static BisGene getGeneDomain(String geneName, String chr, int pos) {
        ArrayList<BisGene> domainMap = geneDomainMap.get(geneName);

        if (domainMap != null) {
            for (BisGene domain : domainMap) {
                if (domain.isPositionIncluded(chr, pos)) {
                    return domain;
                }
            }
        }

        return null;
    }

    public static BisGene getExonDomain(String geneName, String chr, int pos) {
        ArrayList<BisGene> exonMap = geneExonMap.get(geneName);

        if (exonMap != null) {
            for (BisGene exon : exonMap) {
                if (exon.isPositionIncluded(chr, pos)) {
                    return exon;
                }
            }
        }

        return null;
    }
}
