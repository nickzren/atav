package function.annotation.base;

import function.genotype.collapsing.CollapsingCommand;
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

    private static HashMap<String, HashSet<Gene>> geneMap = new HashMap<>();
    private static HashMap<String, StringBuilder> chrAllGeneMap = new HashMap<>();
    private static HashMap<String, HashSet<Gene>> geneMapByName = new HashMap<>();
    private static final HashMap<String, HashSet<Gene>> geneMapByBoundary = new HashMap<>();

    private static ArrayList<Gene> geneBoundaryList = new ArrayList<>();
    private static int allGeneBoundaryLength;

    private static HashMap<String, String> geneCoverageSummaryMap = new HashMap<>();
    private static boolean isUsed = false;

    public static void init() throws Exception {
        initGeneName();

        initGeneBoundaries();

        initGeneMap();

        resetRegionList();
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
                Gene gene = new Gene(geneName);
                if (gene.isValid()) {
                    HashSet<Gene> set = new HashSet<>();
                    set.add(gene);
                    geneMapByName.put(geneName, set);
                } else {
                    LogManager.writeAndPrint("Invalid gene: " + gene.getName());
                }
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

                Gene gene = new Gene(lineStr);
                if (gene.isValid()) {
                    HashSet<Gene> set = new HashSet<>();
                    set.add(gene);

                    geneMapByName.put(lineStr, set);
                } else {
                    LogManager.writeAndPrint("Invalid gene: " + gene.getName());
                }
            }
        } catch (Exception e) {
            ErrorManager.print("\nError line ("
                    + lineNum + ") in gene file: " + lineStr, ErrorManager.INPUT_PARSING);
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

        int geneIndex = 0;
        allGeneBoundaryLength = 0;

        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                line = line.replaceAll("\"", "").replaceAll("\t", " ");

                Gene gene = new Gene(line);

                HashSet<Gene> set = new HashSet<>();
                set.add(gene);

                String geneId = gene.getName();

                if (geneId.contains("_")) { // if using gene domain
                    String geneName = geneId.substring(0, geneId.indexOf("_"));

                    if (!geneMapByBoundary.containsKey(geneName)) {
                        geneMapByBoundary.put(geneName, set);
                    } else {
                        geneMapByBoundary.get(geneName).add(gene);
                    }
                } else {
                    geneMapByBoundary.put(geneId, set);
                }

                gene.setIndex(geneIndex++);
                geneBoundaryList.add(gene);
                allGeneBoundaryLength += gene.getLength();
            }
        }
    }

    private static void initGeneMap() {
        if (isUsed) {
            if (geneMapByName.isEmpty()) {
                geneMap.putAll(geneMapByBoundary);
            } else if (geneMapByBoundary.isEmpty()) {
                geneMap.putAll(geneMapByName);
            } else {
                HashSet<String> nameSet = new HashSet<>();

                nameSet.addAll(geneMapByName.keySet());
                nameSet.addAll(geneMapByBoundary.keySet());

                for (String geneName : nameSet) {
                    if (geneMapByName.containsKey(geneName)
                            && geneMapByBoundary.containsKey(geneName)) {
                        HashSet<Gene> set = geneMapByBoundary.get(geneName);

                        geneMap.put(geneName, set);
                    }
                }
            }
        }
    }

    private static void resetRegionList() throws Exception {
        if (isUsed) {

            ArrayList<String> chrList = new ArrayList<>();

            for (String chr : RegionManager.ALL_CHR) {
                chrAllGeneMap.put(chr, new StringBuilder());
            }

            geneMap.entrySet().stream().forEach((entry) -> {
                Gene gene = entry.getValue().iterator().next();
                if (!gene.getChr().isEmpty()) {
                    if (!chrList.contains(gene.getChr())) {
                        chrList.add(gene.getChr());
                    }

                    StringBuilder sb = chrAllGeneMap.get(gene.getChr());
                    if (sb.length() == 0) {
                        sb.append("'").append(entry.getKey()).append("'");
                    } else {
                        sb.append(",'").append(entry.getKey()).append("'");
                    }
                }
            });

            if (!RegionManager.isUsed()) {
                RegionManager.clear();
                RegionManager.initChrRegionList(chrList.toArray(new String[chrList.size()]));
                RegionManager.sortRegionList();
            }
        }
    }

    public static String getAllGeneByChr(String chr) {
        return "(" + chrAllGeneMap.get(chr) + ",'')";
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

    public static HashMap<String, HashSet<Gene>> getMap() {
        return geneMap;
    }

    public static ArrayList<Gene> getGeneBoundaryList() {
        return geneBoundaryList;
    }

    public static int getAllGeneBoundaryLength() {
        return allGeneBoundaryLength;
    }

    public static boolean isValid(Annotation annotation) {
        if (geneMap.isEmpty()) {
            return true;
        }

        HashSet<Gene> set = geneMap.get(annotation.geneName);

        if (set != null) {
            if (GeneManager.getGeneBoundaryList().isEmpty()) {
                return true;
            } else {
                for (Gene gene : set) {
                    if (gene.contains(annotation.region)) {
                        annotation.geneDomainName = gene.getName(); // set gene domain name
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isUsed() {
        return isUsed;
    }
}
