package function.annotation.base;

import function.cohort.collapsing.CollapsingCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import function.variant.base.RegionManager;
import global.Data;
import java.sql.Statement;
import java.util.StringJoiner;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class GeneManager {

    public static final String TMP_GENE_TABLE = "tmp_gene_chr"; // need to append chr in real time
    public static final String HGNC_GENE_MAP_PATH = "data/gene/hgnc_gene_map_121118.tsv";

    private static HashMap<String, HashSet<Gene>> geneMap = new HashMap<>();
    private static HashMap<String, StringJoiner> chrAllGeneMap = new HashMap<>();
    private static HashMap<String, HashSet<Gene>> geneMapByName = new HashMap<>();
    private static final HashMap<String, HashSet<Gene>> geneMapByBoundaries = new HashMap<>();
    // key: existing dragendb gene name, value: up to date gene name
    private static HashMap<String, String> hgncGeneMap = new HashMap<>();

    private static ArrayList<Gene> geneBoundaryList = new ArrayList<>();
    private static int allGeneBoundaryLength;

    private static HashMap<String, String> geneCoverageSummaryMap = new HashMap<>();
    public static String geneCoverageSummaryHeader = "";
    private static boolean isUsed = false;

    private static boolean hasGeneDomainInput = false;

    public static void init() throws Exception {
        initHgncGeneMap();

        initGeneName();

        initGeneBoundaries();

        initGeneMap();

        resetRegionList();

        initTempTable();
    }

    private static void initHgncGeneMap() {
        try {
            File file = new File(Data.ATAV_HOME + HGNC_GENE_MAP_PATH);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] tmp = line.split("\t");

                    hgncGeneMap.put(tmp[0], tmp[1]);
                }
            }
            br.close();
            fr.close();
        } catch (IOException ex) {
            ErrorManager.send(ex);
        }
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
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

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

            br.close();
            fr.close();
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

        int geneIndex = 0;
        allGeneBoundaryLength = 0;

        File f = new File(AnnotationLevelFilterCommand.geneBoundaryFile);
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String line;
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                line = line.replaceAll("\"", "").replaceAll("\t", " ");

                String[] fields = line.split("( )+");
                if (!RegionManager.isChrValid(fields[1])) {
                    ErrorManager.print("Invalid gene boundary: " + line, ErrorManager.INPUT_PARSING);
                }

                Gene gene = new Gene(line);

                if (gene.isValid()) {
                    HashSet<Gene> set = new HashSet<>();
                    set.add(gene);

                    String geneId = gene.getName();

                    if (geneId.contains("_")) { // if using gene domain
                        hasGeneDomainInput = true;

                        String geneName = geneId.substring(0, geneId.indexOf("_"));

                        if (!geneMapByBoundaries.containsKey(geneName)) {
                            geneMapByBoundaries.put(geneName, set);
                        } else {
                            geneMapByBoundaries.get(geneName).add(gene);
                        }
                    } else {
                        geneMapByBoundaries.put(geneId, set);
                    }

                    gene.setIndex(geneIndex++);
                    geneBoundaryList.add(gene);
                    allGeneBoundaryLength += gene.getLength();
                } else {
                    LogManager.writeAndPrint("Invalid gene: " + gene.getName());
                }
            }
        }

        br.close();
        fr.close();
    }

    private static void initGeneMap() {
        if (isUsed) {
            if (geneMapByName.isEmpty()) {
                geneMap.putAll(geneMapByBoundaries);
            } else if (geneMapByBoundaries.isEmpty()) {
                geneMap.putAll(geneMapByName);
            } else {
                HashSet<String> nameSet = new HashSet<>();

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
            ArrayList<String> chrList = new ArrayList<>();

            for (String chr : RegionManager.ALL_CHR) {
                chrAllGeneMap.put(chr, new StringJoiner(","));
            }

            geneMap.entrySet().stream().forEach((entry) -> {
                Gene gene = entry.getValue().iterator().next();
                if (!gene.getChr().isEmpty()) {
                    if (!chrList.contains(gene.getChr())) {
                        chrList.add(gene.getChr());
                    }

                    chrAllGeneMap.get(gene.getChr()).add("('" + entry.getKey() + "')");
                }
            });

            if (!RegionManager.isUsed()) {
                RegionManager.clear();
                RegionManager.initChrRegionList(chrList.toArray(new String[chrList.size()]));
                RegionManager.sortRegionList();
            }
        }
    }

    private static void initTempTable() {
        try {
            for (String chr : chrAllGeneMap.keySet()) {
                Statement stmt = DBManager.createStatementByConcurReadOnlyConn();

                // create table
                stmt.executeUpdate(
                        "CREATE TEMPORARY TABLE " + TMP_GENE_TABLE + chr + "("
                        + "input_gene varchar(128) NOT NULL, "
                        + "PRIMARY KEY (input_gene)) ENGINE=MEMORY;");

                if (chrAllGeneMap.get(chr).length() > 0) {
                    // insert values
                    stmt.executeUpdate("INSERT IGNORE INTO " + TMP_GENE_TABLE + chr
                            + " values " + chrAllGeneMap.get(chr).toString());

                    stmt.closeOnCompletion();
                }
            }

            chrAllGeneMap.clear(); // free memmory
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void initCoverageSummary() throws Exception {
        if (CollapsingCommand.coverageSummaryFile.isEmpty()) {
            return;
        }

        boolean isHeader = true;
        File f = new File(CollapsingCommand.coverageSummaryFile);
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String line;
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                int firstCommaIndex = line.indexOf(",");
                String firstRowValue = line.substring(0, firstCommaIndex);
                String restRowValues = line.substring(firstCommaIndex + 1);

                if (isHeader) {
                    isHeader = false;

                    geneCoverageSummaryHeader = restRowValues;
                }

                if (!geneCoverageSummaryMap.containsKey(firstRowValue)) {
                    geneCoverageSummaryMap.put(firstRowValue, restRowValues);
                }
            }
        }

        br.close();
        fr.close();
    }

    public static void addCoverageSummary(String geneName, StringJoiner sj) {
        if (geneCoverageSummaryMap.containsKey(geneName)) {
            sj.add(geneCoverageSummaryMap.get(geneName));
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

    public static boolean isValid(Annotation annotation, String chr, int pos) {
        if (geneMap.isEmpty()) {
            return true;
        }

        HashSet<Gene> set = geneMap.get(annotation.geneName);

        if (set != null) {
            if (GeneManager.getGeneBoundaryList().isEmpty()) {
                return true;
            } else {
                for (Gene gene : set) {
                    if (gene.contains(chr, pos)) {
                        // reset gene name to gene domain name so the downstream procedure could match correctly
                        // only for gene boundary input
                        annotation.geneName = gene.getName();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isValid(String geneName, String chr, int pos) {
        if (geneMap.isEmpty()) {
            return true;
        }

        HashSet<Gene> set = geneMap.get(geneName);

        if (set != null) {
            if (GeneManager.getGeneBoundaryList().isEmpty()) {
                return true;
            } else {
                for (Gene gene : set) {
                    if (gene.contains(chr, pos)) {
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

    public static boolean hasGeneDomainInput() {
        return hasGeneDomainInput;
    }

    public static String getUpToDateGene(String dragendbGene) {
        String upToDateGene = hgncGeneMap.get(dragendbGene);
        return upToDateGene == null ? dragendbGene : upToDateGene;
    }
}
