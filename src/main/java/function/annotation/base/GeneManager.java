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
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import utils.CommonCommand;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class GeneManager {

    public static final String TMP_GENE_TABLE = "tmp_gene_chr"; // need to append chr in real time
    public static final String HGNC_GENE_MAP_PATH = "data/gene/hgnc_gene_map_040320.tsv.gz";
    public static final String ALL_GENE_SYMBOL_MAP_PATH = "data/gene/hgnc_complete_set_to_GRCh37.87_040320.tsv.gz";

    private static HashMap<String, HashSet<Gene>> geneMap = new HashMap<>();
    private static HashMap<String, StringJoiner> chrAllGeneMap = new HashMap<>();
    private static HashMap<String, HashSet<Gene>> geneMapByName = new HashMap<>();
    private static final HashMap<String, HashSet<Gene>> geneMapByBoundaries = new HashMap<>();
    // key: existing dragendb gene name, value: up to date gene name
    private static HashMap<String, String> hgncGeneMap = new HashMap<>();
    private static HashMap<String, String> allGeneSymbolMap = new HashMap<>();

    private static ArrayList<Gene> geneBoundaryList = new ArrayList<>();
    private static int allGeneBoundaryLength;

    private static HashMap<String, String> geneCoverageSummaryMap = new HashMap<>();
    public static String geneCoverageSummaryHeader = "";
    private static boolean isUsed = false;

    private static boolean hasGeneDomainInput = false;

    private static PreparedStatement preparedStatement4GeneChrom;

    public static void init() throws Exception {
        if (CommonCommand.isNonDBAnalysis) {
            return;
        }

        initPreparedStatement4GeneChrom();

        initHgncGeneMap();

        initAllGeneSymbolMap();

        initGeneName();

        initGeneBoundaries();

        initGeneMap();

        initAllGeneMapAndResetRegionList();

        initTempTable();
    }

    private static void initPreparedStatement4GeneChrom() {
        String sql = "SELECT chrom FROM hgnc WHERE gene=?";
        preparedStatement4GeneChrom = DBManager.initPreparedStatement(sql);
    }

    public static PreparedStatement getPreparedStatement4GeneChrom() {
        return preparedStatement4GeneChrom;
    }

    private static void initHgncGeneMap() {
        try {
            File f = new File(Data.ATAV_HOME + HGNC_GENE_MAP_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] tmp = line.split("\t");

                    hgncGeneMap.put(tmp[0], tmp[1]);
                }
            }
            br.close();
            decoder.close();
            in.close();
        } catch (IOException ex) {
            ErrorManager.send(ex);
        }
    }

    private static void initAllGeneSymbolMap() {
        try {
            File f = new File(Data.ATAV_HOME + ALL_GENE_SYMBOL_MAP_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] tmp = line.split("\t");

                    allGeneSymbolMap.put(tmp[0], tmp[1]);
                }
            }

            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            ErrorManager.send(e);
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
                }

                if (!gene.isExist()) {
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
                }

                if (!gene.isExist()) {
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

        Reader decoder;

        if (f.getName().endsWith(".gz")) {
            InputStream fileStream = new FileInputStream(f);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            decoder = new InputStreamReader(gzipStream);
        } else {
            decoder = new FileReader(f);
        }

        BufferedReader br = new BufferedReader(decoder);

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

                        geneId = geneId.substring(0, geneId.indexOf("_"));
                    }

                    if (!geneMapByBoundaries.containsKey(geneId)) {
                        geneMapByBoundaries.put(geneId, set);
                    } else {
                        geneMapByBoundaries.get(geneId).add(gene);
                    }

                    gene.setIndex(geneIndex++);
                    geneBoundaryList.add(gene);
                    allGeneBoundaryLength += gene.getLength();
                }

                if (!gene.isExist()) {
                    LogManager.writeAndPrint("Invalid gene: " + gene.getName());
                }
            }
        }

        br.close();
        decoder.close();

        if (geneBoundaryList.isEmpty()) {
            ErrorManager.print("--gene-boundary input does not have any valid data.", ErrorManager.INPUT_PARSING);
        }
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

    private static void initAllGeneMapAndResetRegionList() throws Exception {
        if (isUsed) {
            geneMap.entrySet().stream().forEach((entry) -> {
                for (Gene gene : entry.getValue()) {
                    if (!gene.getChr().isEmpty()) {
                        chrAllGeneMap.putIfAbsent(gene.getChr(), new StringJoiner(","));
                        chrAllGeneMap.get(gene.getChr()).add("('" + entry.getKey() + "')");
                    }
                }
            });

            RegionManager.clear();
            RegionManager.initChrRegionList(chrAllGeneMap.keySet().toArray(new String[chrAllGeneMap.keySet().size()]));
            RegionManager.sortRegionList();
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

        if (geneCoverageSummaryMap.isEmpty()) {
            LogManager.writeAndPrint("--read-coverage-summary input does not have any valid data.");
        }
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

    public static String getGeneDomainName(String geneName, String chr, int pos) {
        if (hasGeneDomainInput) {
            HashSet<Gene> set = GeneManager.getMap().get(geneName);

            if (set != null) {
                for (Gene gene : set) {
                    if (gene.contains(chr, pos)) {
                        return gene.getName();
                    }
                }
            }
        }

        return geneName;
    }

    public static String getUpToDateGene(String dragendbGene) {
        String upToDateGene = hgncGeneMap.get(dragendbGene);
        return upToDateGene == null ? dragendbGene : upToDateGene;
    }

    public static String getAllGeneSymbol(List<String> geneList) {
        if (geneList.isEmpty()) {
            return Data.STRING_NA;
        }

        StringJoiner sj = new StringJoiner(";");

        for (String gene : geneList) {
            String allGeneSymbol = allGeneSymbolMap.get(gene);
            sj.add(allGeneSymbol == null ? gene : allGeneSymbol);
        }

        return sj.toString();
    }
}
