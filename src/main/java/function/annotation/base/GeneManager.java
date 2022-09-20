package function.annotation.base;

import function.cohort.collapsing.CollapsingCommand;
import function.external.acmg.ACMGCommand;
import function.external.acmg.ACMGManager;
import function.external.clingen.ClinGen;
import function.external.clingen.ClinGenCommand;
import function.external.clingen.ClinGenManager;
import function.external.omim.OMIMCommand;
import function.external.omim.OMIMManager;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import function.variant.base.RegionManager;
import function.variant.base.VariantLevelFilterCommand;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import utils.CommonCommand;
import utils.DBManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class GeneManager {

    public static final String TMP_GENE_TABLE = "tmp_gene_chr"; // need to append chr in real time
    public static final String HGNC_GENE_MAP_PATH = "data/gene/hgnc_gene_map_040320.tsv.gz";
    public static final String ALL_GENE_SYMBOL_MAP_PATH = "data/gene/hgnc_complete_set_to_GRCh37.87_040320.tsv.gz";
    public static final String ALL_GENE_TRANSCRIPT_COUNT_MAP_PATH = "data/gene/gencode_gene_transcript_count_v24lift37.tsv.gz";
    public static final String TTN_TRANSCRIPTS_PSI_PATH = "data/gene/ttn_transcripts_psi.csv.gz";
    
    private static int[][] ttnTranscriptsLowPSIRegionArray = new int[176][2]; // tnn transcript data has exact 176 records' PSI < 90

    private static HashMap<String, HashSet<Gene>> geneMap = new HashMap<>();
    private static HashMap<String, StringJoiner> chrAllGeneMap = new HashMap<>();
    private static HashMap<String, HashSet<Gene>> geneMapByName = new HashMap<>();
    private static final HashMap<String, HashSet<Gene>> geneMapByBoundaries = new HashMap<>();
    // key: existing dragendb gene name, value: up to date gene name
    private static HashMap<String, String> hgncGeneMap = new HashMap<>();
    private static HashMap<String, String> allGeneSymbolMap = new HashMap<>();
    private static HashMap<String, Integer> allGeneTranscriptCountMap = new HashMap<>();

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

        initAllGeneTranscriptCountMap();

        initGeneName();

        initGeneBoundaries();

        initGeneMap();

        resetGeneMap();

        initAllGeneMapAndResetRegionList();

        initTempTable();
        
        initTTNTranscriptsLowPSIRegionArray();
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

    private static void initAllGeneTranscriptCountMap() {
        try {
            File f = new File(Data.ATAV_HOME + ALL_GENE_TRANSCRIPT_COUNT_MAP_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] tmp = line.split("\t");

                    allGeneTranscriptCountMap.put(tmp[0], Integer.valueOf(tmp[1]));
                }
            }

            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initTTNTranscriptsLowPSIRegionArray() {
        if (!VariantLevelFilterCommand.isIncludeTTNLowPSI) {
            return;
        }

        try {
            File f = new File(Data.ATAV_HOME + TTN_TRANSCRIPTS_PSI_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String line = "";
            int index = 0; // the source data listed pair coordinates order from high to low
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("Exon Number")) {
                    String[] tmp = line.split(",");

                    float psi = Float.parseFloat(tmp[3]);

                    if (psi < 90) {
                        ttnTranscriptsLowPSIRegionArray[index][0] = Integer.parseInt(tmp[2]); // Hg19 end is actually start pos
                        ttnTranscriptsLowPSIRegionArray[index++][1] = Integer.parseInt(tmp[1]); // Hg19 start is actually end pos
                    }
                }
            }

            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getTTNLowPSI(String geneName, int effectID, int pos) {
        if (!VariantLevelFilterCommand.isIncludeTTNLowPSI) {
            return Data.STRING_NA;
        }

        if (!geneName.equals("TTN")) {
            return Data.STRING_NA;
        }

        if(!EffectManager.isLOF(effectID)) {
            return Data.STRING_NA;
        }
        
        return isLowPSIRegion(pos) ? "1" : Data.STRING_NA;
    }

    private static boolean isLowPSIRegion(int pos) {
        for (int i = 0; i < ttnTranscriptsLowPSIRegionArray.length; i++) {
            if(pos >= ttnTranscriptsLowPSIRegionArray[i][0]
                    && pos <= ttnTranscriptsLowPSIRegionArray[i][1]) {
                return true;
            }
        }

        return false;
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

    public static void resetGeneMap() {
        if (ACMGCommand.isOnly) {
            geneMap.clear();
            for (String geneName : ACMGManager.getAllGeneSet()) {
                Gene gene = new Gene(geneName);

                if (gene.isValid()) {
                    HashSet<Gene> set = new HashSet<>();
                    set.add(gene);
                    geneMap.put(geneName, set);
                }
            }
        } else if (ClinGenCommand.isHaploinsufficiencyOnly) {
            geneMap.clear();
            for (Map.Entry<String, ClinGen> entry : ClinGenManager.getMap().entrySet()) {
                if (entry.getValue().isInClinGenSufficientEvidence()) {
                    Gene gene = new Gene(entry.getKey());

                    if (gene.isValid()) {
                        HashSet<Gene> set = new HashSet<>();
                        set.add(gene);
                        geneMap.put(gene.getName(), set);
                    }
                }
            }
        } else if (ClinGenCommand.isRecessiveOnly) {
            geneMap.clear();
            for (Map.Entry<String, ClinGen> entry : ClinGenManager.getMap().entrySet()) {
                if (entry.getValue().isInClinGenRecessiveEvidence()) {
                    Gene gene = new Gene(entry.getKey());

                    if (gene.isValid()) {
                        HashSet<Gene> set = new HashSet<>();
                        set.add(gene);
                        geneMap.put(gene.getName(), set);
                    }
                }
            }
        } else if (OMIMCommand.isOnly) {
            geneMap.clear();
            for (String geneName : OMIMManager.getAllGeneSet()) {
                Gene gene = new Gene(geneName);

                if (gene.isValid()) {
                    HashSet<Gene> set = new HashSet<>();
                    set.add(gene);
                    geneMap.put(geneName, set);
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

            RegionManager.initOrResetChrRegionList(chrAllGeneMap.keySet().toArray(new String[chrAllGeneMap.keySet().size()]));
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
                        + "PRIMARY KEY (input_gene));");

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

    public static boolean isValid(Annotation annotation, String chr, int pos, int indelLength) {
        if (geneMap.isEmpty()) {
            return true;
        }

        HashSet<Gene> set = geneMap.get(annotation.geneName);

        if (set != null) {
            if (GeneManager.getGeneBoundaryList().isEmpty()) {
                return true;
            } else {
                for (Gene gene : set) {
                    // use pos for SNVs, MNVs, Insertion
                    // use pos and pos + indelLength for Deletion (indelLength is negative number here)
                    if (gene.contains(chr, pos)
                            || (indelLength < 0 && gene.contains(chr, pos - indelLength))) {

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

    public static boolean isValid(String geneName, String chr, int pos, int indelLength) {
        if (geneMap.isEmpty()) {
            return true;
        }

        HashSet<Gene> set = geneMap.get(geneName);

        if (set != null) {
            if (GeneManager.getGeneBoundaryList().isEmpty()) {
                return true;
            } else {
                for (Gene gene : set) {
                    if (gene.contains(chr, pos)
                            || (indelLength < 0 && gene.contains(chr, pos - indelLength))) {
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

    public static String getAllGeneSymbol(Set<String> geneList) {
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

    public static String getAllGeneTranscriptCount(Map<String, Integer> geneTranscriptCountMap) {
        if (geneTranscriptCountMap.isEmpty()) {
            return Data.STRING_NA;
        }

        StringJoiner sj = new StringJoiner(";");

        geneTranscriptCountMap.forEach((k, v) -> sj.add(
                k + ":" + v + "/" + FormatManager.getInteger(allGeneTranscriptCountMap.get(k))));

        return sj.toString();
    }
}
