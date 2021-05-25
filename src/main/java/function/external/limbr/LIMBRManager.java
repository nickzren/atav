package function.external.limbr;

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
public class LIMBRManager {

    private static final String DOMAIN_PATH = "data/limbr/DomainScoresAndPercentilesPathogenic_8-6-19.txt.gz";
    private static final String EXON_PATH = "data/limbr/ExonScoresAndPercentilesPathogenic_8-6-19.txt.gz";

    private static HashMap<String, ArrayList<LIMBRGene>> geneDomainMap = new HashMap<>();
    private static HashMap<String, ArrayList<LIMBRGene>> geneExonMap = new HashMap<>();

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("LIMBR Domain Name");
        sj.add("LIMBR Domain Score");
        sj.add("LIMBR Domain Percentile");
        sj.add("LIMBR Exon Name");
        sj.add("LIMBR Exon Score");
        sj.add("LIMBR Exon Percentile");

        return sj.toString();
    }

    public static String getVersion() {
        return "LIMBR: " + DataManager.getVersion(EXON_PATH) + "\n";
    }

    public static void init() {
        if (LIMBRCommand.isInclude) {
            initGeneMap(geneDomainMap, Data.ATAV_HOME + DOMAIN_PATH);

            initGeneMap(geneExonMap, Data.ATAV_HOME + EXON_PATH);
        }
    }

    private static void initGeneMap(HashMap<String, ArrayList<LIMBRGene>> geneMap, String filePath) {
        try {
            File f = new File(filePath);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);
            
            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("Gene")) {
                    continue;
                }

                String[] tmp = lineStr.split("\t");

                String geneName = tmp[0];
                String id = tmp[1];
                String[] regionStr = tmp[2].split(":");
                String chr = regionStr[0];
                ArrayList<Region> regionList = getRegionList(regionStr[1]);
                float score = FormatManager.getFloat(tmp[3]);
                float percentiles = FormatManager.getFloat(tmp[4]);

                LIMBRGene limbrGene = new LIMBRGene(id, chr, regionList,
                        score, percentiles);

                if (geneMap.containsKey(geneName)) {
                    geneMap.get(geneName).add(limbrGene);
                } else {
                    ArrayList<LIMBRGene> idArray = new ArrayList<>();
                    idArray.add(limbrGene);
                    geneMap.put(geneName, idArray);
                }
            }

            br.close();
            decoder.close();
            in.close();
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

    public static LIMBRGene getGeneDomain(String geneName, String chr, int pos) {
        ArrayList<LIMBRGene> domainMap = geneDomainMap.get(geneName);

        if (domainMap != null) {
            for (LIMBRGene domain : domainMap) {
                if (domain.isPositionIncluded(chr, pos)) {
                    return domain;
                }
            }
        }

        return null;
    }

    public static LIMBRGene getExonDomain(String geneName, String chr, int pos) {
        ArrayList<LIMBRGene> exonMap = geneExonMap.get(geneName);

        if (exonMap != null) {
            for (LIMBRGene exon : exonMap) {
                if (exon.isPositionIncluded(chr, pos)) {
                    return exon;
                }
            }
        }

        return null;
    }
}
