package function.annotation.base;

import function.genotype.collapsing.CollapsingCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import function.variant.base.RegionManager;
import java.sql.Statement;
import java.util.StringJoiner;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class GeneManager {

    public static final String TMP_GENE_TABLE = "tmp_gene_chr"; // need to append chr in real time

    private static HashMap<String, HashSet<Gene>> geneMap = new HashMap<>();
    private static HashMap<String, StringJoiner> chrAllGeneMap = new HashMap<>();
    private static HashMap<String, HashSet<Gene>> geneMapByName = new HashMap<>();
    private static final HashMap<String, HashSet<Gene>> geneMapByBoundaries = new HashMap<>();

    private static ArrayList<Gene> geneBoundaryList = new ArrayList<>();
    private static int allGeneBoundaryLength;

    private static HashMap<String, String> geneCoverageSummaryMap = new HashMap<>();
    public static String geneCoverageSummaryTitle = "";
    private static boolean isUsed = false;

    private static boolean hasGeneDomainInput = false;

    public static void init() throws Exception {
        initGeneName();

        initGeneBoundaries();

        initGeneMap();

        resetRegionList();

        initTempTable();
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
                Statement stmt = DBManager.createStatementByReadOnlyConn();

                // create table
                stmt.executeUpdate(
                        "CREATE TEMPORARY TABLE " + TMP_GENE_TABLE + chr + "("
                        + "input_gene varchar(128) NOT NULL, "
                        + "PRIMARY KEY (input_gene)) ENGINE=TokuDB;");

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

        boolean isTitle = true;
        File f = new File(CollapsingCommand.coverageSummaryFile);
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                int firstCommaIndex = line.indexOf(",");
                String firstRowValue = line.substring(0, firstCommaIndex);
                String restRowValues = line.substring(firstCommaIndex + 1);

                if (isTitle) {
                    isTitle = false;

                    geneCoverageSummaryTitle = restRowValues;
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
                        annotation.geneName = gene.getName();
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
}
