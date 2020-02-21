package function.external.subrvis;

import function.external.base.DataManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class SubRvisManager {

    private static final String SUBRVIS_DOMAIN_PATH = "data/subrvis/domain_score_052816.txt";
    private static final String SUBRVIS_EXON_PATH = "data/subrvis/exon_score_052816.txt";

    private static HashMap<String, ArrayList<SubRvisGene>> geneDomainMap = new HashMap<>();
    private static HashMap<String, ArrayList<SubRvisGene>> geneExonMap = new HashMap<>();

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("subRVIS Domain Name");
        sj.add("subRVIS Domain Percentile");
        sj.add("MTR Domain Percentile");
        sj.add("subRVIS Exon Name");
        sj.add("subRVIS Exon Percentile");
        sj.add("MTR Exon Percentile");

        return sj.toString();
    }

    public static String getVersion() {
        return "Sub RVIS: " + DataManager.getVersion(SUBRVIS_EXON_PATH) + "\n";
    }

    public static void init() {
        if (SubRvisCommand.isInclude) {
            initGeneMap(geneDomainMap, Data.ATAV_HOME + SUBRVIS_DOMAIN_PATH);

            initGeneMap(geneExonMap, Data.ATAV_HOME + SUBRVIS_EXON_PATH);
        }
    }

    private static void initGeneMap(HashMap<String, ArrayList<SubRvisGene>> geneMap, String filePath) {
        try {
            File f = new File(filePath);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            
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
                float percentile = FormatManager.getFloat(tmp[3]);
                float mtrPercentile = FormatManager.getFloat(tmp[4]);

                SubRvisGene subRvisGene = new SubRvisGene(id, chr, regionList, percentile, mtrPercentile);

                if (geneMap.containsKey(geneName)) {
                    geneMap.get(geneName).add(subRvisGene);
                } else {
                    ArrayList<SubRvisGene> idArray = new ArrayList<>();
                    idArray.add(subRvisGene);
                    geneMap.put(geneName, idArray);
                }
            }
            
            br.close();
            fr.close();
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
