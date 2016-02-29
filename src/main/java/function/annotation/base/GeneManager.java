package function.annotation.base;

import function.coverage.base.Gene;
import function.genotype.collapsing.CollapsingCommand;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import function.variant.base.RegionManager;

/**
 *
 * @author nick
 */
public class GeneManager {

    private static HashMap<String, HashSet<Gene>> geneMap = new HashMap<String, HashSet<Gene>>();
    private static HashMap<String, HashSet<Gene>> geneMapByName = new HashMap<String, HashSet<Gene>>();
    private static HashMap<String, HashSet<Gene>> geneMapByBoundaries = new HashMap<String, HashSet<Gene>>();

    private static HashMap<String, String> geneCoverageSummaryMap = new HashMap<String, String>();
    private static HashMap<String, Integer> artifactsGeneMap = new HashMap<String, Integer>();
    private static HashMap<String, String> genenStableIdNmNpMap = new HashMap<String, String>();
    private static boolean isUsed = false;

    public static void init() throws Exception {
        initGeneName();

        initGeneBoundaries();

        initGeneMap();

        resetRegionList();

        initArtifactsGeneMap();
    }

    private static void initGeneName() throws Exception {
        if (AnnotationLevelFilterCommand.geneInput.isEmpty()) {
            return;
        }

        isUsed = true;

        File f = new File(AnnotationLevelFilterCommand.geneInput);

        if (f.isFile()) {
            initFromFile(f);
        } else {
            String[] genes = AnnotationLevelFilterCommand.geneInput.split(",");
            for (String geneName : genes) {
                HashSet<Gene> set = new HashSet<Gene>();
                Gene gene = new Gene(geneName);
                set.add(gene);
                geneMapByName.put(geneName, set);
            }
        }
    }

    private static void initFromFile(File f) {
        String lineStr = "";
        int lineNum = 0;

        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                HashSet<Gene> set = new HashSet<Gene>();
                Gene gene = new Gene(lineStr);
                set.add(gene);

                geneMapByName.put(lineStr, set);
            }
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in gene file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    private static void initGeneBoundaries() throws Exception {
        if (AnnotationLevelFilterCommand.geneBoundaryFile.isEmpty()) {
            return;
        }

        isUsed = true;

        File f = new File(AnnotationLevelFilterCommand.geneBoundaryFile);
        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                Gene gene = new Gene(line);
                gene.populateSlaveList();

                HashSet<Gene> set = new HashSet<Gene>();
                set.add(gene);

                String geneId = gene.getName();

                if (geneId.contains("_")) { // if using gene domain
                    String geneName = geneId.substring(0, geneId.indexOf("_"));

                    if (!geneMapByBoundaries.containsKey(geneName)) {
                        geneMapByBoundaries.put(geneName, set);
                    } else {
                        geneMapByBoundaries.get(geneName).add(gene);
                    }
                } else {
                    geneMapByBoundaries.put(geneId, set);
                }
            }
        }
    }

    private static void initGeneMap() {
        if (isUsed) {
            if (geneMapByName.isEmpty()) {
                geneMap.putAll(geneMapByBoundaries);
            } else if (geneMapByBoundaries.isEmpty()) {
                geneMap.putAll(geneMapByName);
            } else {
                HashSet<String> nameSet = new HashSet<String>();

                nameSet.addAll(geneMapByName.keySet());
                nameSet.addAll(geneMapByBoundaries.keySet());

                for (String geneName : nameSet) {
                    if (geneMapByName.containsKey(geneName)
                            && geneMapByBoundaries.containsKey(geneName)) {
                        HashSet<Gene> set = geneMapByBoundaries.get(geneName);

                        geneMap.put(geneName, set);
                    }
                }
            }
        }
    }

    private static void resetRegionList() throws Exception {
        if (isUsed) {
            if (!RegionManager.isUsed()) {
                RegionManager.clear();

                ArrayList<String> chrList = new ArrayList<String>();

                for (HashSet<Gene> geneSet : geneMap.values()) {
                    String chr = geneSet.iterator().next().getChrStr();

                    if (!chr.isEmpty()
                            && !chrList.contains(chr)) {
                        chrList.add(chr);
                    }
                }

                RegionManager.initChrRegionList(chrList.toArray(new String[chrList.size()]));
                RegionManager.sortRegionList();
            }
        }
    }

    private static void initArtifactsGeneMap() throws Exception {
        String artifactsGeneFile = Data.ARTIFACTS_GENE_PATH;

        if (CommonCommand.isDebug) {
            artifactsGeneFile = Data.RECOURCE_PATH + artifactsGeneFile;
        }

        File f = new File(artifactsGeneFile);
        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                String[] temp = line.split("\t");

                artifactsGeneMap.put(temp[0], Integer.valueOf(temp[1]));
            }
        }
    }

    public static Integer getGeneArtifacts(String geneName) {
        Integer temp = artifactsGeneMap.get(geneName);

        if (temp != null) {
            return temp;
        }

        return Data.NA;
    }

    public static void initCoverageSummary() throws Exception {
        if (CollapsingCommand.coverageSummaryFile.isEmpty()) {
            return;
        }

        File f = new File(CollapsingCommand.coverageSummaryFile);
        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        boolean isTitle = true;

        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                int firstCommaIndex = line.indexOf(",");
                String firstRowValue = line.substring(0, firstCommaIndex);
                String restRowValues = line.substring(firstCommaIndex + 1);

                if (isTitle) {
                    isTitle = false;

                    geneCoverageSummaryMap.put("title", restRowValues);
                }

                if (!geneCoverageSummaryMap.containsKey(firstRowValue)) {
                    geneCoverageSummaryMap.put(firstRowValue, restRowValues);
                }
            }
        }
    }

    public static String getCoverageSummary(String geneName) {
        if (geneCoverageSummaryMap.containsKey(geneName)) {
            return geneCoverageSummaryMap.get(geneName);
        } else {
            return "";
        }
    }

    public static void initGeneStableIdNmNpMap() {
        try {
            String geneEnsemblFile = Data.GENE_ENSEMBL_PATH;

            if (CommonCommand.isDebug) {
                geneEnsemblFile = Data.RECOURCE_PATH + geneEnsemblFile;
            }

            File f = new File(geneEnsemblFile);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] temp = line.split("\t");

                    genenStableIdNmNpMap.put(temp[4], temp[3] + "," + temp[5]);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getNmNpValuesByStableId(String stableId) {
        String value = genenStableIdNmNpMap.get(stableId);

        if (value == null) {
            return "NA,NA";
        } else {
            return value;
        }
    }

    public static HashMap<String, HashSet<Gene>> getMap() {
        return geneMap;
    }

    public static boolean isValid(Annotation annotation) {
        if (geneMap.isEmpty()) {
            return true;
        }

        HashSet<Gene> set = geneMap.get(annotation.geneName);

        if (set != null) {
            for (Gene gene : set) {
                if (gene.contains(annotation.region)) {
                    annotation.geneName = gene.getName();
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isValid(String name) {
        if (geneMap.isEmpty()) {
            return true;
        } else {
            return geneMap.containsKey(name);
        }
    }

    public static boolean isUsed() {
        return isUsed;
    }
}
